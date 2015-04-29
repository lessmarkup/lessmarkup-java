package com.lessmarkup.engine.module;

import com.lessmarkup.dataobjects.Module;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.framework.helpers.LoggingHelper;
import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.module.ModuleConfiguration;
import com.lessmarkup.interfaces.module.ModuleInitializer;
import com.lessmarkup.interfaces.module.ModuleProvider;
import com.lessmarkup.interfaces.structure.NodeHandler;
import com.lessmarkup.interfaces.structure.Tuple;
import org.springframework.stereotype.Component;

import java.io.*;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class ModuleProviderImpl implements ModuleProvider {

    private final List<ModuleConfiguration> modules = new ArrayList<>();
    private final Map<String, Tuple<Class<? extends NodeHandler>, String>> nodeHandlers = new HashMap<>();

    @Override
    public Collection<ModuleConfiguration> getModules() {
        return modules;
    }

    @Override
    public void discoverAndRegisterModules() {
        
        URLClassLoader classLoader = (URLClassLoader) getClass().getClassLoader();

        for (URL url : classLoader.getURLs()) {
            if (!Objects.equals(url.getProtocol(), "file")) {
                continue;
            }
            if (url.getPath().endsWith(".jar")) {
                // we only load from file system to ignore dependent JARs
                continue;
            }
            discoverModules(url, true, classLoader);
        }

        String modulesPath = RequestContextHolder.getContext().getEngineConfiguration().getModulesPath();
        
        if (modulesPath == null || modulesPath.length() == 0) {
            return;
        }
        
        File modulesDirectory = new File(modulesPath);

        if (modulesDirectory.exists()) {
            for (File file : modulesDirectory.listFiles()) {
                if (!file.getName().endsWith(".jar")) {
                    continue;
                }
                try {
                    discoverModules(file.toURI().toURL(), false, null);
                } catch (MalformedURLException ex) {
                    LoggingHelper.logException(getClass(), ex);
                }
            }
        }
    }
    
    private void listAllFileSystemModuleElements(String basePath, File directory, List<String> elements) {

        File[] files = directory.listFiles();
        if (files == null) {
            String userName = System.getProperty("user.name");
            try (PrintWriter writer = new PrintWriter(System.out)) {
                writer.write(userName);
            }
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                listAllFileSystemModuleElements(basePath, file, elements);
            } else {
                String filePath;
                try {
                    filePath = file.toURI().toURL().getPath();
                    if (filePath.startsWith(basePath)) {
                        elements.add(filePath.substring(basePath.length()));
                    }
                } catch (MalformedURLException ex) {
                    LoggingHelper.logException(getClass(), ex);
                }
            }
        }
    }

    private List<String> listAllFileSystemModuleElements(String path) {
        List<String> ret = new LinkedList<>();
        File moduleDirectory = new File(path);
        try {
            listAllFileSystemModuleElements(moduleDirectory.toURI().toURL().getPath(), moduleDirectory, ret);
        } catch (MalformedURLException ex) {
            LoggingHelper.logException(getClass(), ex);
        }
        return ret;
    }

    private List<String> listAllJarModuleElements(String path) {
        try (InputStream stream = new FileInputStream(path); ZipInputStream zip = new ZipInputStream(stream)) {
            List<String> ret = new LinkedList<>();
            for (;;) {
                ZipEntry entry = zip.getNextEntry();
                if (entry == null) {
                    break;
                }

                ret.add(entry.getName());
            }

            return ret;
        } catch (IOException ex) {
            LoggingHelper.logException(getClass(), ex);
            return null;
        }
    }

    private List<String> listAllModuleElements(String path) {
        if (path.endsWith(".jar")) {
            return listAllJarModuleElements(path);
        }

        return listAllFileSystemModuleElements(path);
    }

    private void constructModule(URL moduleUrl, boolean isSystem, String classPath, ClassLoader classLoader, List<String> elements) {

        if (!classPath.endsWith(".class")) {
            return;
        }
        
        ModuleInitializer moduleInitializer;

        try {
            String className = classPath.substring(0, classPath.length()-".class".length()).replaceAll("/", ".");
            Class<?> type = Class.forName(className, true, classLoader);
            if (type.isInterface() || Modifier.isAbstract(type.getModifiers()) || !ModuleInitializer.class.isAssignableFrom(type)) {
                return;
            }
            moduleInitializer = (ModuleInitializer) DependencyResolver.resolve(type);
            if (moduleInitializer == null) {
                return;
            }
        } catch (SecurityException | ClassNotFoundException | IllegalArgumentException ex) {
            LoggingHelper.logException(getClass(), ex);
            return;
        }

        moduleInitializer.initialize();

        ModuleConfigurationImpl moduleConfiguration = new ModuleConfigurationImpl(moduleUrl, isSystem, moduleInitializer.getModuleType(), elements, classLoader, moduleInitializer);

        modules.add(moduleConfiguration);

        for (Class<? extends NodeHandler> nodeHandler : moduleInitializer.getNodeHandlerTypes()) {
            nodeHandlers.put(nodeHandler.getSimpleName(), new Tuple<>(nodeHandler, moduleConfiguration.getModuleType()));
        }
    }
    
    private void discoverModules(URL moduleUrl, boolean isSystem, ClassLoader classLoader) {

        if (!moduleUrl.getProtocol().equals("file")) {
            return;
        }

        List<String> elements = listAllModuleElements(moduleUrl.getPath());
        List<String> moduleInitializerClasses = elements.stream()
                .filter(path -> path.endsWith("ModuleInitializer.class"))
                .collect(Collectors.toCollection(LinkedList::new));

        if (moduleInitializerClasses.isEmpty()) {
            LoggingHelper.getLogger(getClass()).info(String.format("Cannot find initalizer for module %s", moduleUrl));
            return;
        }

        if (classLoader == null) {
            classLoader = URLClassLoader.newInstance(new URL[]{moduleUrl}, getClass().getClassLoader());
        }

        for (String initializerClass : moduleInitializerClasses) {
            constructModule(moduleUrl, isSystem, initializerClass, classLoader, elements);
        }
    }

    @Override
    public void updateModuleDatabase(DomainModelProvider domainModelProvider) {
        if (domainModelProvider == null) {
            return;
        }
        
        try (DomainModel domainModel = domainModelProvider.create()) {
            List<ModuleConfiguration> existingModules = new ArrayList<>();
            domainModel.query().from(Module.class).where("removed = $", false).toList(Module.class).forEach(module -> {
                Optional<ModuleConfiguration> reference = modules.stream().filter(m -> m.getUrl().toString().equals(module.getPath())).findFirst();
                if (!reference.isPresent()) {
                    module.setRemoved(true);
                } else {
                    existingModules.add(reference.get());
                    module.setSystem(reference.get().isSystem());
                    module.setModuleType(reference.get().getModuleType());
                }
                domainModel.update(module);
            });
            
            modules.stream().filter(m -> !existingModules.contains(m)).forEach(configuration -> {
                Module module = new Module();
                module.setEnabled(true);
                module.setName(configuration.getInitializer().getName());
                module.setPath(configuration.getUrl().toString());
                module.setRemoved(false);
                module.setSystem(configuration.isSystem());
                module.setModuleType(configuration.getModuleType());
                domainModel.create(module);
            });
        }
    }

    @Override
    public Collection<String> getNodeHandlers() {
        return nodeHandlers.keySet();
    }

    @Override
    public Tuple<Class<? extends NodeHandler>, String> getNodeHandler(String id) {
        return nodeHandlers.get(id);
    }
}

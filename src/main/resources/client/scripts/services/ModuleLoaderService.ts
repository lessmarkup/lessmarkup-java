import ng = require('angular');

class ModuleLoaderService {

    private providers: {};
    private application: ng.IModule;
    private rootElement: ng.IRootElementService;

    private static INVOKE_QUEUE:string = "_invokeQueue";
    private static ENTRIES_INVOKED:string = "__entriesInvoked";
    private static RUN_BLOCKS:string = "_runBlocks";
    private static PROPERTY_REQUIRES:string = "requires";

    public initialize(application: ng.IModule,
        controllerProvider: ng.IControllerProvider,
        compileProvider: ng.ICompileProvider,
        filterProvider: ng.IFilterProvider,
        provide: ng.auto.IProvideService,
        rootElement: ng.IRootElementService
    ) {

        this.application = application;
        this.rootElement = rootElement;
        this.application.requires = [];

        this.providers = {
            $controllerProvider: controllerProvider,
            $compileProvider: compileProvider,
            $filterProvider: filterProvider,
            $provide: provide,
        };

        var requires = [];
        this.fillRequires(this.application, requires);
        ng.forEach(requires, (name: string) => {
            if (!name || name.trim().length == 0) {
                return;
            }
            var module = ng.module(name);
            if (module.hasOwnProperty(ModuleLoaderService.INVOKE_QUEUE)) {
                module[ModuleLoaderService.ENTRIES_INVOKED] = module[ModuleLoaderService.INVOKE_QUEUE].length;
            }
        });
    }

    private getInstanceInjector() : ng.auto.IInjectorService {
        return this.rootElement.data('$injector');
    }

    private runModuleInvokeQueue(module: ng.IModule) {

        if (!module) {
            return;
        }

        if (module.hasOwnProperty(ModuleLoaderService.RUN_BLOCKS)) {

            var runBlocks: any[] = module[ModuleLoaderService.RUN_BLOCKS];
            module[ModuleLoaderService.RUN_BLOCKS] = [];

            ng.forEach(runBlocks, (runBlock) => {
                this.getInstanceInjector().invoke(runBlock);
            });
        }

        if (module.hasOwnProperty(ModuleLoaderService.INVOKE_QUEUE)) {
            var invokeQueue = module[ModuleLoaderService.INVOKE_QUEUE];

            var entriesInvoked = 0;
            if (module.hasOwnProperty(ModuleLoaderService.ENTRIES_INVOKED)) {
                entriesInvoked = module[ModuleLoaderService.ENTRIES_INVOKED];
            }

            for (var i = entriesInvoked; i < invokeQueue.length; i++) {
                var invokeArgs = invokeQueue[i];
                if (this.providers.hasOwnProperty(invokeArgs[0])) {
                    var provider = this.providers[invokeArgs[0]];
                    provider[invokeArgs[1]].apply(provider, invokeArgs[2]);
                }
            }

            module[ModuleLoaderService.ENTRIES_INVOKED] = invokeQueue.length;
        }
    }

    private fillRequires(module: ng.IModule, requires: string[]) {

        //if (module.name === "ui.bootstrap") {
            // it is loaded during startup
            //return;
        //}

        if (!module.hasOwnProperty(ModuleLoaderService.PROPERTY_REQUIRES)) {
            return;
        }

        if (requires.indexOf(module.name) < 0) {
            requires.push(module.name);
        }

        ng.forEach(module.requires, (name: string) => {
            var childModule:ng.IModule = ng.module(name);
            if (!childModule) {
                return;
            }
            this.fillRequires(childModule, requires);
        });
    }

    public loadModules() {

        var requires = [];
        this.fillRequires(this.application, requires);

        ng.forEach(requires, (name: string) => {
            if (!name || name.trim().length == 0) {
                return;
            }
            this.runModuleInvokeQueue(ng.module(name));
        });
    }

    public requireModule(name: string) {
        if (this.application.requires.indexOf(name) >= 0) {
            return;
        }

        this.application.requires.push(name);
    }
}

import module = require('./module');
module.service('moduleLoader', [ModuleLoaderService]);

export = ModuleLoaderService;

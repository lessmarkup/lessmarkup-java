import ng = require('angular');

class PendingModuleLoader {

    private providers: {};
    private app: IApplication;

    private static INVOKE_QUEUE:string = "_invokeQueue";
    private static ENTRIES_INVOKED:string = "__entriesInvoked";
    private static RUN_BLOCKS:string = "_runBlocks";
    private static PROPERTY_REQUIRED:string = "required";

    constructor(controllerProvider: ng.IControllerProvider,
                compileProvider: ng.ICompileProvider,
                filterProvider: ng.IFilterProvider,
                provide: ng.auto.IProvideService) {

        this.providers = {
            $controllerProvider: controllerProvider,
            $compileProvider: compileProvider,
            $filterProvider: filterProvider,
            $provide: provide,
        };
    }

    private static getInstanceInjector() : ng.auto.IInjectorService {

        return ng.injector();
    }

    private runModuleInvokeQueue(module: ng.IModule) {

        if (!module) {
            return;
        }

        if (module.hasOwnProperty(PendingModuleLoader.RUN_BLOCKS)) {

            var runBlocks: string[] = module[PendingModuleLoader.RUN_BLOCKS];
            module[PendingModuleLoader.RUN_BLOCKS] = [];

            ng.forEach(runBlocks, (runBlock) => {
                PendingModuleLoader.getInstanceInjector().invoke(runBlock);
            });
        }

        if (module.hasOwnProperty(PendingModuleLoader.INVOKE_QUEUE)) {
            var invokeQueue = module[PendingModuleLoader.INVOKE_QUEUE];

            var entriesInvoked = 0;
            if (module.hasOwnProperty(PendingModuleLoader.ENTRIES_INVOKED)) {
                entriesInvoked = module[PendingModuleLoader.ENTRIES_INVOKED];
            }

            for (var i = entriesInvoked; i < invokeQueue.length; i++) {
                var invokeArgs = invokeQueue[i];
                if (this.providers.hasOwnProperty(invokeArgs[0])) {
                    var provider = this.providers[invokeArgs[0]];
                    provider[invokeArgs[1]].apply(provider, invokeArgs[2]);
                }
            }

            module[PendingModuleLoader.ENTRIES_INVOKED] = invokeQueue.length;
        }
    }

    public initialize(app: IApplication) {
        this.app = app;
        var requires = [];
        this.fillRequires(app, requires);
        ng.forEach(requires, (name: string) => {
            if (!name || name.trim().length == 0) {
                return;
            }
            var module = ng.module(name);
            if (module.hasOwnProperty(PendingModuleLoader.INVOKE_QUEUE)) {
                module[PendingModuleLoader.ENTRIES_INVOKED] = module[PendingModuleLoader.INVOKE_QUEUE].length;
            }
        });
    }

    private fillRequires(module: ng.IModule, requires: string[]) {

        //if (module.name === "ui.bootstrap") {
            // it is loaded during startup
            //return;
        //}

        if (!module.hasOwnProperty(PendingModuleLoader.PROPERTY_REQUIRED)) {
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
        this.fillRequires(this.app, requires);

        ng.forEach(requires, (name: string) => {
            if (!name || name.trim().length == 0) {
                return;
            }
            this.runModuleInvokeQueue(ng.module(name));
        });
    }
}

import ModuleLoaderService = require('./ModuleLoaderService');

class ModuleLoaderServiceProvider implements ng.IServiceProvider {
    private application: IApplication;
    private controllerProvider: ng.IControllerProvider;
    private provideService: ng.auto.IProvideService;
    private compileProvider: ng.ICompileProvider;
    private filterProvider: ng.IFilterProvider;

    constructor(controllerProvider: ng.IControllerProvider, provideService: ng.auto.IProvideService, compileProvider: ng.ICompileProvider, filterProvider: ng.IFilterProvider) {
        this.controllerProvider = controllerProvider;
        this.provideService = provideService;
        this.compileProvider = compileProvider;
        this.filterProvider = filterProvider;
    }

    initialize(app: IApplication) {
        this.application = app;
    }

    $get = ['$rootElement', (rootElement: ng.IRootElementService) => {
        return new ModuleLoaderService(this.application, this.controllerProvider, this.compileProvider, this.filterProvider, this.provideService, rootElement);
    }]
}

import module = require('./module');
module.provider('moduleLoader', ['$controllerProvider', '$provide', '$compileProvider', '$filterProvider', ModuleLoaderServiceProvider]);

export = ModuleLoaderServiceProvider;

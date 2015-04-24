///<amd-dependency path="angular.material" />
///<amd-dependency path="controllers/index" />
///<amd-dependency path="directives/index" />
///<amd-dependency path="services/index" />

import ng = require('angular');
import ngRoute = require('angular.route');

var appModule = <IApplication> ng.module('app', ['angular.material', 'app.controllers', 'app.directives', 'app.services']);

appModule.config([
        '$controllerProvider',
        '$compileProvider',
        '$routeProvider',
        '$filterProvider',
        '$provide',
        'moduleLoader',
        (controllerProvider: ng.IControllerProvider,
         compileProvider: ng.ICompileProvider,
         routeProvider: ngRoute.IRouteProvider,
         filterProvider: ng.IFilterProvider,
         provide: ng.auto.IProvideService,
         moduleLoader: ModuleLoaderService) => {

    /*appModule.addRouteWhen = (path:string, route:ngRoute.IRoute):void => {

        routeProvider.when(path, route);
    };

    appModule.addRouteOtherwise = (route:ngRoute.IRoute):void => {

        routeProvider.otherwise(route);
    };*/

    moduleLoader.initialize(appModule, controllerProvider, compileProvider, filterProvider, provide);
}]);

export = appModule;

///<amd-dependency path="../controllers/index" />

import ng = require('angular');
import ngRoute = require('angular.route');

var appModule = <IApplication> ng.module('lmApp', ['lmApp.controllers']);

appModule.config([
        '$controllerProvider',
        '$compileProvider',
        '$routeProvider',
        '$filterProvider',
        '$provide',
        (controllerProvider: ng.IControllerProvider,
         compileProvider: ng.ICompileProvider,
         routeProvider: ngRoute.IRouteProvider,
         filterProvider: ng.IFilterProvider,
         provide: ng.auto.IProvideService) => {

    /*appModule.addRouteWhen = (path:string, route:ngRoute.IRoute):void => {

        routeProvider.when(path, route);
    };

    appModule.addRouteOtherwise = (route:ngRoute.IRoute):void => {

        routeProvider.otherwise(route);
    };*/

    var pendingModuleLoader = new PendingModuleLoader(controllerProvider, compileProvider, filterProvider, provide);
    pendingModuleLoader.initialize(appModule);

    appModule.loadPendingModules = ():void => {

        pendingModuleLoader.loadModules();
    }
}]);

export = appModule;

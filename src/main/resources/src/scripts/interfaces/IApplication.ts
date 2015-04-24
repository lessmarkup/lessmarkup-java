import ng = require('angular');
import ngRoute = require('angular.route');

interface IApplication extends ng.IModule {
    //addRouteWhen(path: string, route: ngRoute.IRoute): void;
    //addRouteOtherwise(route: ngRoute.IRoute): void;
    loadPendingModules(): void;
    requireModule(name: string): void;
    requires: string[];
}

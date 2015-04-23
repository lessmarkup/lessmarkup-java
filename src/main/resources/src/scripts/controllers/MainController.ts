/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import ng = require('angular');

interface MainControllerScope extends ng.IScope {
    toolbarButtons: ToolbarButton[];
    title: string;
    breadcrumbs: Breadcrumb[];
    alerts: Alert[];
    showXsMenu: boolean;
    loadingNewPage: boolean;
}

class MainController {
    private configurationPath: string;
    private staticNodes: string[];
    private templates: string[] = [];
    private scope: MainControllerScope;
    private viewData: any = null;
    private serverConfiguration: ServerConfiguration;

    constructor($scope: MainControllerScope, $http: ng.IHttpService, commandHandler: IEventHandler, inputForm: InputForm, $location: ng.ILocationService,
        $browser: ng.IBrowserService, $timeout: ng.ITimeoutService, $sce: ng.ISCEService, serverConfiguration: ServerConfiguration, initialData: InitialData) {

        this.scope = $scope;
        this.serverConfiguration = serverConfiguration;
        this.initializeScope(initialData);
    }

    private initializeScope(initialData: InitialData) {
        this.scope.toolbarButtons = [];
        this.scope.title = "";
        this.scope.breadcrumbs = [];
        this.scope.alerts = [];
        this.scope.showXsMenu = false;
        this.scope.title = this.serverConfiguration.rootTitle;
        this.scope.loadingNewPage = true;
    }

    resetAlerts(): void {
        this.scope.alerts = [];
    }

    showError(message:string):void {
    }
}

import appControllers = require('lmApp.controllers');

appControllers.controller('lmMain', [
    '$scope',
    '$http',
    'commandHandler',
    'inputForm',
    '$location',
    '$browser',
    '$timeout',
    '$sce',
    'serverConfiguration',
    'initialData',
    MainController]);

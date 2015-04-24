/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import ng = require('angular');

interface MainControllerScope extends ng.IScope {
    showXsMenu: boolean;
    hideXsMenu: () => void;
    template: string;
    configuration: any;
}

class MainController {
    private configurationPath: string;
    private staticNodes: string[];
    private templates: string[] = [];
    private scope: MainControllerScope;
    private viewData: any = null;

    constructor($scope: MainControllerScope, initialData: InitialData, nodeLoader: NodeLoaderService, messaging: MessagingService) {
        this.scope = $scope;
        this.initializeScope(initialData);

        if (initialData.nodeLoadError && initialData.nodeLoadError.length > 0) {
            messaging.showError(initialData.nodeLoadError);
        } else {
            nodeLoader.onNodeLoaded(initialData.loadedNode, initialData.path);
        }

        this.scope.$on(BroadcastEvents.NODE_LOADED, (event: ng.IAngularEvent, configuration: NodeConfiguration) => {
            this.scope.configuration = configuration.viewData;
            this.scope.template = configuration.template;
        });
    }

    private initializeScope(initialData: InitialData) {
        this.scope.showXsMenu = false;
        this.scope.hideXsMenu = () => {
            if (this.scope.showXsMenu) {
                this.scope.$apply((scope: ng.IScope) => {
                    this.scope.showXsMenu = false;
                });
            }
        }
    }
}

import appControllers = require('app.controllers');

appControllers.controller('main', [
    '$scope',
    'initialData',
    'nodeLoader',
    'messaging',
    MainController]);

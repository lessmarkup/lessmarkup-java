/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import NodeLoaderService = require('../services/nodeLoader/NodeLoaderService');
import MessagingService = require('../services/MessagingService');
import BroadcastEvents = require('../interfaces/BroadcastEvents');
import NodeConfiguration = require('../datatypes/NodeConfiguration');

interface MainControllerScope extends ng.IScope {
    template: string;
    configuration: any;
    lockSidenav: boolean;
    toggleSidenav: () => void;
    title: string;
}

class MainController {
    private configurationPath: string;
    private staticNodes: string[];
    private templates: string[] = [];
    private scope: MainControllerScope;
    private viewData: any = null;
    private sidenavService: angular.material.MDSidenavService;

    constructor(
        $scope: MainControllerScope,
        initialData: InitialData,
        nodeLoader: NodeLoaderService,
        messaging: MessagingService,
        sidenavService: angular.material.MDSidenavService) {

        this.scope = $scope;
        this.sidenavService = sidenavService;
        this.scope.template = '';
        this.scope.title = '';
        this.scope.configuration = '';
        this.initializeScope(initialData);

        this.scope.$on(BroadcastEvents.NODE_LOADED, (event: ng.IAngularEvent, configuration: NodeConfiguration) => {
            this.scope.configuration = configuration.viewData;
            this.scope.template = configuration.template;
            this.scope.title = configuration.title;
        });

        if (initialData.nodeLoadError && initialData.nodeLoadError.length > 0) {
            messaging.showError(initialData.nodeLoadError);
        } else {
            nodeLoader.onNodeLoaded(initialData.loadedNode, initialData.path);
        }
    }

    private initializeScope(initialData: InitialData) {
        this.scope.lockSidenav = false;
        this.scope.toggleSidenav = () => this.sidenavService('left').toggle();
    }
}

import module = require('./module');

module.controller('main', [
    '$scope',
    'initialData',
    'nodeLoader',
    'messaging',
    '$mdSidenav',
    MainController
]);

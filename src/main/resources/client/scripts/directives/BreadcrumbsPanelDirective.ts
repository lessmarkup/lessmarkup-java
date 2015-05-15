/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import BroadcastEvents = require('../interfaces/BroadcastEvents');
import _ = require('lodash');

interface BreadcrumbsPanelDirectiveScope extends ng.IScope {
    breadcrumbs: Breadcrumb[];
    title: string;
}

class BreadcrumbsPanelDirectiveController {

    private scope: BreadcrumbsPanelDirectiveScope;

    constructor(scope: BreadcrumbsPanelDirectiveScope, initialData: InitialData) {
        this.scope = scope;

        if (_.isObject(initialData.loadedNode)) {
            this.scope.breadcrumbs = initialData.loadedNode.breadcrumbs;
        }

        this.scope.$on(BroadcastEvents.NODE_LOADED, (event: ng.IAngularEvent, nodeLoad: NodeLoadData) => {
            this.onNodeLoaded(nodeLoad);
        });
    }

    private onNodeLoaded(nodeLoad: NodeLoadData) {
        this.scope.breadcrumbs = nodeLoad.breadcrumbs;
        this.scope.title = nodeLoad.title;
    }
}

import module = require('./module');

module.directive('breadcrumbsPanel', ['serverConfiguration', (serverConfiguration: ServerConfiguration) => {
    return <ng.IDirective> {
        templateUrl: serverConfiguration.rootPath + '/views/breadcrumbsPanel.html',
        restrict: 'E',
        replace: true,
        scope: true,
        controller: ['$scope', 'initialData', BreadcrumbsPanelDirectiveController]
    };
}]);

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import MessagingService = require('../services/MessagingService');

interface NodeLinkDirectiveScope extends ng.IScope {
    path: string;
    fullPath: string;
}

class NodeLinkDirectiveController {
    constructor(scope: NodeLinkDirectiveScope, serverConfiguration: ServerConfiguration) {
        if (scope.path === '' || scope.path === '/') {
            scope.fullPath = serverConfiguration.rootPath;
        } else {
            scope.fullPath = serverConfiguration.rootPath + scope.path;
        }
    }
}

import NodeLoaderService = require('../services/nodeLoader/NodeLoaderService');

class NodeLinkDirectiveLink {

    private nodeLoader: NodeLoaderService;
    private scope: NodeLinkDirectiveScope;
    private messagingService: MessagingService;
    private element: JQuery;

    constructor(scope: NodeLinkDirectiveScope, element: JQuery, nodeLoader: NodeLoaderService, messagingService: MessagingService){
        this.scope = scope;
        this.nodeLoader = nodeLoader;
        this.messagingService = messagingService;
        this.element = element;

        element.on('click', (e: JQueryEventObject) => {
            e.preventDefault();
            this.clickHandler();
        });
    }

    private clickHandler() {
        this.nodeLoader.loadNode(this.scope.path)
            .catch((message: string) => {
                this.messagingService.showError(message);
            });
        return null;
    }
}

import module = require('./module');

module.directive('nodeLink', ['nodeLoader', 'messaging', (nodeLoader: NodeLoaderService, messagingService: MessagingService) => {
    return <ng.IDirective>{
        template: '<a class="{{class}}" href="{{fullPath}}"><ng-transclude></ng-transclude></a>',
        restrict: 'EA',
        transclude: true,
        replace: true,
        scope: {
            path: '=',
            class: '@'
        },
        controller: ['$scope', 'serverConfiguration', NodeLinkDirectiveController],
        link: (scope: NodeLinkDirectiveScope, element: JQuery) => {
            new NodeLinkDirectiveLink(scope, element, nodeLoader, messagingService);
        }
    };
}]);

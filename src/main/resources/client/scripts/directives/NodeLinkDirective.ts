/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

interface NodeLinkDirectiveScope extends ng.IScope {
    path: string;
    fullPath: string;
}

class NodeLinkDirectiveController {
    constructor(scope: NodeLinkDirectiveScope, serverConfiguration: ServerConfiguration) {
        scope.fullPath = serverConfiguration.rootPath + '/' + scope.path;
    }
}

import NodeLoaderService = require('../services/nodeLoader/NodeLoaderService');

class NodeLinkDirectiveLink {

    private nodeLoader: NodeLoaderService;
    private scope: NodeLinkDirectiveScope;

    clickHandler(): JQuery {
        this.nodeLoader.loadNode(this.scope.path);
        return null;
    }

    constructor(scope: NodeLinkDirectiveScope, element: JQuery, nodeLoader: NodeLoaderService){
        this.scope = scope;
        this.nodeLoader = nodeLoader;
        element.click = () => this.clickHandler();
    }
}

import module = require('./module');

module.directive('nodeLink', ['nodeLoader', (nodeLoader: NodeLoaderService) => {
    return <ng.IDirective>{
        template: '<a class="{{class}}" href="{{fullPath}}"><ng-transclude></ng-transclude></a>',
        restrict: 'E',
        transclude: true,
        replace: true,
        scope: {
            path: '=',
            class: '@'
        },
        controller: ['$scope', 'serverConfiguration', NodeLinkDirectiveController],
        link: (scope: NodeLinkDirectiveScope, element: JQuery) => {
            new NodeLinkDirectiveLink(scope, element, nodeLoader);
        }
    };
}]);

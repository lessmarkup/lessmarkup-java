/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import NodeLoaderService = require('../services/nodeLoader/NodeLoaderService');

interface NavigationTreeDirectiveScope extends ng.IScope {
    navigateRoot: () => void;
    tree: MenuItem[];
    navigateItem: (item: MenuItem) => void;
    rootTitle: string;
}

class NavigationTreeDirectiveLink {
    constructor(scope:NavigationTreeDirectiveScope, elem:JQuery, nodeLoader:NodeLoaderService, serverConfiguration: ServerConfiguration) {
        scope.navigateRoot = () => nodeLoader.loadNode('/');
        scope.tree = serverConfiguration.navigationTree;
        scope.navigateItem = (item: MenuItem) => nodeLoader.loadNode(item.url);
        scope.rootTitle = serverConfiguration.rootTitle;
    }
}

import module = require('./module');

module.directive('navigationTree', [
    'serverConfiguration',
    'nodeLoader',
    (serverConfiguration:ServerConfiguration, nodeLoader: NodeLoaderService) => {

        return <ng.IDirective> {
            templateUrl: serverConfiguration.rootPath + '/views/navigationTree.html',
            restrict: 'E',
            replace: true,
            scope: true,
            link: (scope:NavigationTreeDirectiveScope, elem:JQuery) => {
                new NavigationTreeDirectiveLink(scope, elem, nodeLoader, serverConfiguration);
            }
        };
    }]);

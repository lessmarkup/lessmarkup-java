/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

interface CollectionUpdatesDirectiveScope extends ng.IScope {
    getTemplateUrl(): string;
    platform: PlatformType;
    collections: CollectionDefinition[];
    gotoCollection(collection: CollectionDefinition): void;
    hasCollections(): boolean;
    collectionClass(collection: CollectionDefinition): string;
}

class CollectionUpdatesDirective {

    private static VIEW_PATH_MOBILE = "/views/collectionUpdatesMobile.html";
    private static VIEW_PATH_NORMAL = "/views/collectionUpdates.html";

    constructor($scope: CollectionUpdatesDirectiveScope, serverConfiguration: ServerConfiguration, collectionUpdates: CollectionUpdatesService, nodeLoader: NodeLoaderService) {

        $scope.getTemplateUrl = () => {
            return $scope.platform === PlatformType.DESKTOP ?
                CollectionUpdatesDirective.VIEW_PATH_NORMAL : CollectionUpdatesDirective.VIEW_PATH_MOBILE;
        };

        $scope.collections = collectionUpdates.getCollections();

        $scope.$watch(
            ():number => { return collectionUpdates.getVersion() },
            ():void => { $scope.collections = collectionUpdates.getCollections(); }
        );

        $scope.gotoCollection = (collection : CollectionDefinition) => {
            nodeLoader.loadNode(collection.path);
        };

        $scope.hasCollections = () => {
            return $scope.collections && $scope.collections.length > 0;
        };

        $scope.collectionClass = (collection : CollectionDefinition) => {
            if (collection.count > 0) {
                return "active-notification";
            }
            return "";
        }
    }
}

import module = require('./module');

module.directive('collectionUpdates', [() => {
    return <ng.IDirective>{
        template: '<ng-include src="getTemplateUrl()"/>',
        restrict: 'E',
        replace: true,
        scope: {
            platform: '='
        },
        controller: ['$scope', 'serverConfiguration', 'collectionUpdates', 'nodeLoader', CollectionUpdatesDirective]
    };
}]);

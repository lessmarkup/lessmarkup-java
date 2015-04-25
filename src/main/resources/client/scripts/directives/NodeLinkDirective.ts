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

    clickHandler(eventObject: JQueryEventObject) {
        this.nodeLoader.loadNode(this.scope.path);
    }

    constructor(scope: NodeLinkDirectiveScope, element: JQuery, attrs, nodeLoader: NodeLoaderService){
        this.scope = scope;
        this.nodeLoader = nodeLoader;
        element.click(this.clickHandler);
    }
}

import module = require('./module');

module.directive('nodeLink', [() => {
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
        link: ['nodeLoader', NodeLinkDirectiveLink]
    };
}]);

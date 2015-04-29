interface NavigationTreeDirectiveScope extends ng.IScope {

}

class NavigationTreeDirectiveLink {
    constructor(scope: NavigationTreeDirectiveScope, elem: JQuery, attrs) {

    }
}

import module = require('./module');

module.directive('navigationTree', ['serverConfiguration', (serverConfiguration: ServerConfiguration) => {
    return <ng.IDirective> {
        templateUrl: serverConfiguration.rootPath + '/views/navigationTree.html',
        restrict: 'E',
        replace: true,
        scope: true,
        link: ['$scope', NavigationTreeDirectiveLink]
    };
}]);

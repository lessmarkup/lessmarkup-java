interface NavigationTreeDirectiveScope extends ng.IScope {

}

class NavigationTreeDirectiveLink {
    constructor(scope: NavigationTreeDirectiveScope, elem: JQuery, attrs) {

    }
}

import module = require('./module');

module.directive('navigationTree', [() => {
    return <ng.IDirective> {
        templateUrl: '/views/navigationTree.html',
        restrict: 'E',
        replace: true,
        scope: true,
        link: ['$scope', NavigationTreeDirectiveLink]
    };
}]);

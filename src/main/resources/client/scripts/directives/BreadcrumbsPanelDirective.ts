interface BreadcrumbsPanelDirectiveScope extends ng.IScope {

}

class BreadcrumbsPanelDirectiveController {
    constructor(scope: BreadcrumbsPanelDirectiveScope) {

    }
}

import module = require('./module');

module.directive('breadcrumbsPanel', [() => {
    return <ng.IDirective> {
        templateUrl: '/views/breadcrumbsPanel.html',
        restrict: 'E',
        replace: true,
        scope: true,
        controller: ['$scope', BreadcrumbsPanelDirectiveController]
    };
}]);

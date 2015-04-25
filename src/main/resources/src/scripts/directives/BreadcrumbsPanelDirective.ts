interface BreadcrumbsPanelDirectiveScope extends ng.IScope {

}

class BreadcrumbsPanelDirectiveController {
    constructor(scope: BreadcrumbsPanelDirectiveScope) {

    }
}

module.directive('breadcrumbsPanel', [() => {
    return <ng.IDirective> {
        templateUrl: '/views/breadcrumbsPanel.html',
        restrict: 'E',
        replace: true,
        scope: true,
        controller: ['$scope', BreadcrumbsPanelDirectiveController]
    };
}]);

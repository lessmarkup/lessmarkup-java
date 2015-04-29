interface BreadcrumbsPanelDirectiveScope extends ng.IScope {

}

class BreadcrumbsPanelDirectiveController {
    constructor(scope: BreadcrumbsPanelDirectiveScope) {

    }
}

import module = require('./module');

module.directive('breadcrumbsPanel', ['serverConfiguration', (serverConfiguration: ServerConfiguration) => {
    return <ng.IDirective> {
        templateUrl: serverConfiguration.rootPath + '/views/breadcrumbsPanel.html',
        restrict: 'E',
        replace: true,
        scope: true,
        controller: ['$scope', BreadcrumbsPanelDirectiveController]
    };
}]);

interface SearchPanelDirectiveScope extends ng.IScope {

}

class SearchPanelDirectiveLink {
    constructor(scope: SearchPanelDirectiveScope, elem: JQuery, attrs) {

    }
}

import module = require('./module');

module.directive('searchPanel', [() => {
    return <ng.IDirective> {
        templateUrl: '/views/searchPanel.html',
        restrict: 'E',
        replace: true,
        scope: true,
        link: [SearchPanelDirectiveLink]
    };
}]);

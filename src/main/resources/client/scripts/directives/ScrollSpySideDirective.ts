import FlatPageController = require('../controllers/FlatPageController');

function ScrollSpySideDirectiveLink(scope: FlatPageControllerScope) {
    scope.$applyAsync(() => {
        FlatPageController.enableScrollSpy();

        scope.$on(BroadcastEvents.NODE_LOADED, function () {
            FlatPageController.disableScrollSpy();
        });
    });
}

import module = require('./module');

module.directive('scrollSpySide', [() => {
    return <ng.IDirective> {
        link: ScrollSpySideDirectiveLink
    };
}]);

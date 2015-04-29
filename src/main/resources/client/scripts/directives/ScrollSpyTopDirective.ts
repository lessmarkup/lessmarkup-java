import $ = require('jquery');
import FlatPageController = require('../controllers/FlatPageController');
import BroadcastEvents = require('../interfaces/BroadcastEvents');

interface ScrollSpyTopDirectiveScope extends ng.IScope {

}

function ScrollSpyTopDirectiveLink(scope: ScrollSpyTopDirectiveScope, element: JQuery) {
    scope.$applyAsync(() => {
        var navbarMenu = $("#navbar-menu");

        if (!navbarMenu.is(":visible")) {
            navbarMenu = $("#navbar-menu-small");
        }

        var children = element.detach().insertAfter(navbarMenu).addClass("scrollspy");

        FlatPageController.enableScrollSpy();

        scope.$on(BroadcastEvents.NODE_LOADED, function () {
            children.remove();
            FlatPageController.disableScrollSpy();
        });
    });
}

import module = require('./module');

module.directive('scrollSpyTop', [() => {
    return <ng.IDirective> {
        link: ScrollSpyTopDirectiveLink
    };
}]);

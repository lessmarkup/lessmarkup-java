/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import FlatPageController = require('../controllers/FlatPageController');
import BroadcastEvents = require('../interfaces/BroadcastEvents');

function ScrollSpySideDirectiveLink(scope: FlatPageControllerScope) {
    scope.$applyAsync(() => {
        FlatPageController.enableScrollSpy();
        scope.$on(BroadcastEvents.NODE_LOADED, () => FlatPageController.disableScrollSpy());
    });
}

import module = require('./module');

module.directive('scrollSpySide', [() => {
    return <ng.IDirective> {
        link: ScrollSpySideDirectiveLink
    };
}]);

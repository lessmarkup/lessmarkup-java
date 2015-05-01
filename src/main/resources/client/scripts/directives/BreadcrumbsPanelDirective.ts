/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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

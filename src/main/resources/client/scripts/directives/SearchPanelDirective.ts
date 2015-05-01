/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

interface SearchPanelDirectiveScope extends ng.IScope {
}

class SearchPanelDirectiveLink {
    constructor(scope: SearchPanelDirectiveScope, elem: JQuery, attrs) {
    }
}

import module = require('./module');

module.directive('searchPanel', ['serverConfiguration', (serverConfiguration: ServerConfiguration) => {
    return <ng.IDirective> {
        templateUrl: serverConfiguration.rootPath + '/views/searchPanel.html',
        restrict: 'E',
        replace: true,
        scope: true,
        link: [SearchPanelDirectiveLink]
    };
}]);

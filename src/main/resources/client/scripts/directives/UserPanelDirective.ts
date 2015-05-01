/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

interface UserPanelDirectiveScope extends ng.IScope {
    getTemplateUrl(): string;
    platform: PlatformType;
}

class UserPanelDirectiveController {

    private static VIEW_PATH_MOBILE = "/views/userPanelMobile.html";
    private static VIEW_PATH_NORMAL = "/views/userPanel.html";

    constructor($scope: UserPanelDirectiveScope, serverConfiguration: ServerConfiguration) {
        $scope.getTemplateUrl = () => {
            return serverConfiguration.rootPath + ($scope.platform === PlatformType.DESKTOP ?
                UserPanelDirectiveController.VIEW_PATH_NORMAL : UserPanelDirectiveController.VIEW_PATH_MOBILE);
        };
    }
}

import module = require('./module');

module.directive('userPanel', [() => {
    return <ng.IDirective> {
        template: '<ng-include src="getTemplateUrl()"/>',
        restrict: 'E',
        replace: true,
        scope: {
            platform: '='
        },
        controller: ['$scope', 'serverConfiguration', UserPanelDirectiveController]
    };
}]);

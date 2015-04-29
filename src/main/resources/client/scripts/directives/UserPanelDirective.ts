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

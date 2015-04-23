import ng = require('angular');
import app = require('app');

interface UserPanelDirectiveScope extends ng.IScope {
    getTemplateUrl(): string;
    platform: PlatformType;
}

class UserPanelDirective {

    private static VIEW_PATH_MOBILE = "/views/userPanelMobile.html";
    private static VIEW_PATH_NORMAL = "/views/userPanel.html";

    constructor($scope: UserPanelDirectiveScope) {
        $scope.getTemplateUrl = () => {
            return $scope.platform === PlatformType.DESKTOP ?
                UserPanelDirective.VIEW_PATH_NORMAL : UserPanelDirective.VIEW_PATH_MOBILE;
        };
    }
}

app.directive('userPanel', ['', () => {
    return <ng.IDirective>{
        template: '<ng-include src="getTemplateUrl()"/>',
        restrict: 'E',
        replace: true,
        scope: {
            platform: '='
        },
        controller: ['$scope', UserPanelDirective]
    };
}]);
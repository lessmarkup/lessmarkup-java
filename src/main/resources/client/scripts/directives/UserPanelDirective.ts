/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import BroadcastEvents = require('../interfaces/BroadcastEvents');
import NodeLoaderService = require('../services/nodeLoader/NodeLoaderService');
import UserSecurityService = require('../services/userSecurity/UserSecurityService');

interface UserPanelDirectiveScope extends ng.IScope {
    hasConfiguration: boolean;
    hasLogin: boolean;
    loggedIn: boolean;
    openConfiguration: () => void;
}

class UserPanelDirectiveController {

    private scope: UserPanelDirectiveScope;
    private userSecurity: UserSecurityService;

    constructor(scope: UserPanelDirectiveScope, userSecurity: UserSecurityService, serverConfiguration: ServerConfiguration, nodeLoader: NodeLoaderService) {

        this.scope = scope;
        this.userSecurity = userSecurity;
        this.scope.openConfiguration = () => {
            nodeLoader.loadNode(serverConfiguration.configurationPath);
        };

        scope.$on(BroadcastEvents.USER_STATE_CHANGED, () => {
            this.onUserStateChanged();
        });

        this.onUserStateChanged();
        this.scope.hasLogin = serverConfiguration.hasLogin;
    }

    private onUserStateChanged() {
        this.scope.hasConfiguration = this.userSecurity.showConfiguration();
        this.scope.loggedIn = this.userSecurity.isLoggedIn();
    }
}

import module = require('./module');

module.directive('userPanel', ['serverConfiguration', (serverConfiguration: ServerConfiguration) => {
    return <ng.IDirective> {
        templateUrl: serverConfiguration.rootPath + '/views/userPanel.html',
        restrict: 'E',
        replace: true,
        scope: true,
        controller: ['$scope', 'userSecurity', 'serverConfiguration', 'nodeLoader', UserPanelDirectiveController]
    };
}]);

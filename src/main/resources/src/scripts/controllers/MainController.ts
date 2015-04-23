/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import app = require('app');

interface MainControllerScope extends angular.IScope {
    toolbarButtons: ToolbarButton[];
    title: string;
    breadcrumbs: Breadcrumb[];
    loginState: LoginState;
    alerts: string[];
    hasLogin: boolean;
    hasSearch: boolean;
}

app.controller('main', ['$scope', '$http', 'commandHandler', 'inputForm', '$location', '$browser', '$timeout', 'lazyLoad', '$sce', MainController]);

export class MainController {
    configurationPath: string;
    path: string;
    staticNodes: string[];
    templates: string[];

    constructor($scope: MainControllerScope, $http: angular.IHttpService, commandHandler: CommandHandler, inputForm: InputForm, $location: angular.ILocationService,
        $browser: angular.IBrowserService, $timeout: angular.ITimeoutService, lazyLoad: LazyLoad, $sce: angular.ISCEService) {

    }
}
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import DialogController = require('./DialogController');
import UserSecurityService = require('../services/userSecurity/UserSecurityService');
import DialogControllerScope = require('./DialogControllerScope');
import CommandProcessorService = require('../services/CommandProcessorService');
import BroadcastEvents = require('../interfaces/BroadcastEvents');

class LoginModel {
    password: string;
    email: string;
    remember: boolean;
}

interface LoginDialogControllerConfiguration extends DialogControllerConfiguration {
    administratorKey: string;
}

interface LoginDialogControllerScope extends DialogControllerScope {
    configuration: LoginDialogControllerConfiguration;
    loggedIn: boolean;
}

class LoginDialogController extends DialogController {

    private qService2: ng.IQService;
    private userSecurity: UserSecurityService;
    private administratorKey: string;
    private loginScope: LoginDialogControllerScope;

    constructor(scope:LoginDialogControllerScope,
                dialogService:angular.material.MDDialogService,
                sceService:ng.ISCEService,
                serverConfiguration:ServerConfiguration,
                commandProcessor:CommandProcessorService,
                qService:ng.IQService,
                userSecurityService: UserSecurityService) {
        this.administratorKey = scope.configuration.administratorKey;
        super(scope, dialogService, sceService, serverConfiguration, commandProcessor, qService);
        this.qService2 = qService;
        this.userSecurity = userSecurityService;
        this.loginScope = scope;
        this.loginScope.loggedIn = this.userSecurity.isLoggedIn();

        this.loginScope.$on(BroadcastEvents.USER_LOGGED_IN, () => {
            this.loginScope.loggedIn = true;
        });

        this.loginScope.$on(BroadcastEvents.USER_LOGGED_OUT, () => {
            this.loginScope.loggedIn = false;
        })
    }

    protected successFunction(model: LoginModel):ng.IPromise<void> {
        var deferred:ng.IDeferred<void> = this.qService2.defer<void>();

        this.userSecurity.loginUser(model.email, model.password, model.remember, this.administratorKey).then(
            () => {
                deferred.resolve();
                this.loginScope.loggedIn = this.userSecurity.isLoggedIn();
            },
            (message) => {
                deferred.reject(message);
                this.loginScope.loggedIn = this.userSecurity.isLoggedIn();
            }
        );

        return deferred.promise;
    }
}

import module = require('./module');
module.controller("loginDialog", [
    '$scope',
    '$mdDialog',
    '$sce',
    'serverConfiguration',
    'commandProcessor',
    '$q',
    'userSecurity',
    LoginDialogController
]);

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

interface LoginDialogDirectiveScope extends ng.IScope {
    showLogin: () => void;
}

import InputFormService = require('../services/InputFormService');
import UserSecurityService = require('../services/userSecurity/UserSecurityService');
import CommandProcessorService = require('../services/CommandProcessorService');
import MessagingService = require('../services/MessagingService');
import BroadcastEvents = require('../interfaces/BroadcastEvents');

class LoginDialogDirectiveController {

    private inputFormService: InputFormService;
    private loginModelId: string;
    private userSecurity: UserSecurityService;
    private commandProcessor: CommandProcessorService;
    private messagingService: MessagingService;
    private qService: ng.IQService;

    constructor(scope:LoginDialogDirectiveScope,
                inputFormService:InputFormService,
                serverConfiguration:ServerConfiguration,
                userSecurity:UserSecurityService,
                commandProcessor: CommandProcessorService,
                messagingService: MessagingService,
                qService: ng.IQService) {
        scope.$on(BroadcastEvents.INITIATE_USER_LOGIN, this.startLogin);
        scope.showLogin = this.startLogin;
        this.userSecurity = userSecurity;
        this.loginModelId = serverConfiguration.loginModelId;
        this.inputFormService = inputFormService;
        this.commandProcessor = commandProcessor;
        this.messagingService = messagingService;
        this.qService = qService;
    }

    startLogin(): void {
        this.inputFormService.editObject<UserLoginModel>(null, this.loginModelId, (model: UserLoginModel) => {
            var deferred: ng.IDeferred<void> = this.qService.defer<void>();

            this.userSecurity.loginUser(model.email, model.password, model.remember).then(
                () => deferred.resolve(),
                (message) => this.messagingService.showError(message) );

            return deferred.promise;
        });
    }

    registerNewUser(): void {
        this.commandProcessor.sendCommand<RegisterModelRequest>("getRegisterObject", {})
        .then((request: RegisterModelRequest) => {
            var registerObject = request.registerObject;
            var modelId = request.modelId;

            this.inputFormService.editObject(request.registerObject, request.modelId, (registerObject) => {
                var deferred: ng.IDeferred<void> = this.qService.defer<void>();

                this.commandProcessor.sendCommand("register", { user: registerObject }).then(
                    () => deferred.resolve(),
                    (message) => deferred.reject(message)
                );

                return deferred.promise;
            });
        });
    }
}

import module = require('./module');

module.directive('loginDialog', ['serverConfiguration', (serverConfiguration: ServerConfiguration) => {
    return <ng.IDirective>{
        templateUrl: serverConfiguration.rootPath + '/views/login.html',
        restrict: 'E',
        replace: true,
        scope: true,
        controller: ['$scope', 'inputForm', 'serverConfiguration', 'userSecurity', 'commandProcessor', 'messaging', '$q', LoginDialogDirectiveController]
    };
}]);

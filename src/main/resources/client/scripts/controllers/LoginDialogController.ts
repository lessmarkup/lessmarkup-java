import DialogController = require('./DialogController');
import UserSecurityService = require('../services/userSecurity/UserSecurityService');
import DialogControllerScope = require('./DialogControllerScope');
import CommandProcessorService = require('../services/CommandProcessorService');

class LoginModel {
    password: string;
    email: string;
    remember: boolean;
}

class LoginDialogController extends DialogController {

    private qService2: ng.IQService;
    private userSecurity: UserSecurityService;

    constructor(scope:DialogControllerScope,
                dialogService:angular.material.MDDialogService,
                sceService:ng.ISCEService,
                serverConfiguration:ServerConfiguration,
                commandProcessor:CommandProcessorService,
                qService:ng.IQService,
                userSecurityService: UserSecurityService) {
        super(scope, dialogService, sceService, serverConfiguration, commandProcessor, qService);
        this.qService2 = qService;
        this.userSecurity = userSecurityService;
    }

    protected successFunction(model: LoginModel):ng.IPromise<void> {
        var deferred:ng.IDeferred<void> = this.qService2.defer<void>();

        this.userSecurity.loginUser(model.email, model.password, model.remember).then(
            () => deferred.resolve(),
            (message) => deferred.reject(message) );

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
    DialogController
]);

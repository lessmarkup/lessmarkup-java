import ng = require('angular');

interface LoginDialogDirectiveScope extends ng.IScope {
    showLogin: () => void;
}

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
                () => { deferred.resolve(); },
                (message: string) => { this.messagingService.showError(message); });

            return deferred.promise;
        });
    }

    registerNewUser(): void {
        this.commandProcessor.sendCommand<RegisterModelRequest>("getRegisterObject", {})
        .then((request: RegisterModelRequest) => {
            var registerObject = request.registerObject;
            var modelId = request.modelId;

            this.inputFormService.editObject(request.registerObject, request.modelId, (registerObject) => {
                var deferred = ng.IDeferred<void> = this.qService.defer<void>();

                this.commandProcessor.sendCommand("register", { user: registerObject }).then(
                    () => { deferred.resolve(); },
                    (message: string) => { deferred.reject(message); }
                );
            });
        });
    }
}

import app = require('app');

app.directive('loginDialog', ['$scope', 'inputForm', () => {
    return <ng.IDirective>{
        templateUrl: '/views/login.html',
        restrict: 'E',
        replace: true,
        scope: true,
        controller: ['$scope', LoginDialogDirectiveController]
    };
}]);

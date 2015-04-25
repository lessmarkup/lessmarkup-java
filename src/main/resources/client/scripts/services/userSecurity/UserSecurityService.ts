import ng = require('angular');

class LoginStage1Request {
    user: string;
    administratorKey: string;
}

class LoginStage1Response {
    pass1: string;
    pass2: string;
}

class LoginStage2Request {
    user: string;
    hash: string;
    remember: boolean;
    administratorKey: string;
}

class LoginStage2Response {
    loggedIn: boolean;
    userName: string;
    userNotVerified: boolean;
    showConfiguration: boolean;
    path: string;
}

import CommandProcessorService = require('../CommandProcessorService');
import MessagingService = require('../MessagingService');
import NodeLoaderService = require('../nodeLoader/NodeLoaderService');

class UserSecurityService {

    private userState: UserState;
    private version: number = 0;
    private rootScope: ng.IRootScopeService;
    private commandProcessor: CommandProcessorService;
    private messaging: MessagingService;
    private loginProgress: boolean = false;
    private nodeLoader: NodeLoaderService;
    private qService: ng.IQService;

    private static STAGE1_COMMAND_NAME = "loginStage1";

    constructor(rootScope: ng.IRootScopeService, commandProcessor: CommandProcessorService, messaging: MessagingService, nodeLoader: NodeLoaderService, qService: ng.IQService) {
        this.userState = new UserState();
        this.commandProcessor = commandProcessor;
        this.messaging = messaging;
        this.nodeLoader = nodeLoader;
        this.qService = qService;
    }

    public updateState(stateUpdate: UserStateUpdate) {
        if (!stateUpdate.loggedIn) {
            if (this.userState.loggedIn) {
                this.doLogout();
            }
            return;
        }

        var hasChanges = false;

        if (!this.userState.loggedIn) {
            this.userState.loggedIn = true;
            hasChanges = true;
        }

        if (stateUpdate.userName && this.userState.name !== stateUpdate.userName) {
            this.userState.name = stateUpdate.userName;
            hasChanges = true;
        }

        if (stateUpdate.userNotVerified !== this.userState.notVerified) {
            this.userState.notVerified = stateUpdate.userNotVerified;
            hasChanges = true;
        }

        if (this.userState.showConfiguration !== stateUpdate.showConfiguration) {
            this.userState.showConfiguration = stateUpdate.showConfiguration;
            hasChanges = true;
        }

        if (hasChanges) {
            this.version++;
        }
    }

    public getVersion(): number {
        return this.version;
    }

    public getUserState(): UserState {
        return this.userState;
    }

    public showConfiguration() : boolean {
        return this.userState.showConfiguration;
    }

    public isLoggedIn() : boolean {
        return this.userState.loggedIn;
    }

    public isVerified() : boolean {
        return !this.userState.notVerified;
    }

    public getName() : string {
        return this.userState.name;
    }

    public doLogout() {
        if (!this.userState.loggedIn) {
            return;
        }
        this.userState = new UserState();
        this.version++;
        this.rootScope.$broadcast(BroadcastEvents.USER_LOGGED_OUT);
    }

    public doLogin() {
        this.commandProcessor.onUserActivity();
        this.rootScope.$broadcast(BroadcastEvents.INITIATE_USER_LOGIN);
    }

    private addLoginError(message:string, defer: ng.IDeferred<void>) {
        this.userState.loggedIn = false;
        this.loginProgress = false;
        defer.reject(message);
        this.version++;
    }

    private onStage1CommandReceived(email: string, password: string, administratorKey: string, remember: boolean, data: LoginStage1Response, defer: ng.IDeferred<void>): void {
        require(['lib/sha512'], function () {
            var pass1 = CryptoJS.SHA512(data.pass1 + password);
            var pass2 = CryptoJS.SHA512(data.pass2 + pass1);

            var stage2Data: LoginStage2Request = {
                user: email,
                hash: data.pass2 + ';' + pass2,
                remember: remember,
                administratorKey: administratorKey
            };

            this.commandProcessor.sendCommand("loginStage2", stage2Data, function (data) {
                this.onStage2CommandReceived(data, defer);
            }, function (message) {
                this.addLoginError(message, defer);
            });
        });
    }

    private onStage2CommandReceived(data: LoginStage2Response, defer: ng.IDeferred<void>) {
        this.loginProgress = false;
        this.userState = new UserState();
        this.userState.loggedIn = data.loggedIn;
        this.userState.name = data.userName;
        this.userState.notVerified = data.userNotVerified;
        this.userState.showConfiguration = data.showConfiguration;

        this.version++;

        defer.resolve();

        this.rootScope.$broadcast(BroadcastEvents.USER_LOGGED_IN);

        if (data.path && data.path.length > 0) {
            this.nodeLoader.loadNode(data.path);
        }
    }

    public loginUser(email: string, password: string, remember: boolean, administratorKey: string = null): ng.IPromise<void> {
        this.commandProcessor.onUserActivity();

        var defer = this.qService.defer<void>();

        if (email.length == 0 || password.length == 0) {
            var error = "Please fill all required fields";
            defer.reject(error);
            return defer.promise;
        }

        if (!/\b[\w\.-]+@[\w\.-]+\.\w{2,4}\b/ig.test(email)) {
            var error = "Invalid e-mail";
            defer.reject(error);
            return defer.promise;
        }

        this.loginProgress = true;

        var stage1Data:LoginStage1Request = {
            user: email,
            administratorKey: null
        };

        if (administratorKey) {
            stage1Data.administratorKey = administratorKey;
        }

        this.commandProcessor.sendCommand<LoginStage1Response>(UserSecurityService.STAGE1_COMMAND_NAME, stage1Data)
        .then(
            (data: LoginStage1Response) => { this.onStage1CommandReceived(email, password, administratorKey, remember, data, defer); },
            (message: string) => { this.addLoginError(message, defer); }
        );

    }
}

import servicesModule = require('../module');
servicesModule.service('userSecurity', ['$rootScope', 'commandProcessor', 'messaging', 'nodeLoader', '$q', UserSecurityService]);

export = UserSecurityService;
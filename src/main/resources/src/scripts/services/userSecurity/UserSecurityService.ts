class UserSecurityService {

    private userState: UserState;
    private version: number = 0;

    constructor() {
        this.userState = new UserState();
    }

    public updateState(stateUpdate: UserStateUpdate) {
        if (!stateUpdate.loggedIn) {
            if (this.userState.loggedIn) {
                this.userState = new UserState();
                this.version++;
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
}

import servicesModule = require('../module');
servicesModule.service('userSecurity', [UserSecurityService]);

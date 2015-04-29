class BackgroundRefreshService {

    private timeout: ng.ITimeoutService;
    private timeoutCancel: ng.IPromise<any>;
    private lastActivity: number;

    constructor(timeout: ng.ITimeoutService) {
        this.timeout = timeout;
        this.timeoutCancel = null;
        this.lastActivity = new Date().getDate() / 1000;
    }

    public cancelUpdates(): void {
        if (this.timeoutCancel !== null) {
            this.timeout.cancel(this.timeoutCancel);
            this.timeoutCancel = null;
        }
    }

    public onUserActivity (updater: () => void) {
        this.lastActivity = new Date().getTime() / 1000;
        this.subscribeForUpdates(updater);
    }

    private getDynamicDelay() {
        var activityDelayMin = (new Date().getTime() / 1000 - this.lastActivity) / 60;

        if (activityDelayMin < 2) {
            return 30;
        }

        if (activityDelayMin < 5) {
            return 60;
        }

        if (activityDelayMin < 10) {
            return 60 * 2;
        }

        return 60*20;
    }

    public subscribeForUpdates(updater: () => void) {

        this.cancelUpdates();
        var lastDelay = this.getDynamicDelay();
        if (lastDelay > 0) {
            this.timeoutCancel = this.timeout(() => {
                this.timeoutCancel = null;
                updater();
            }, lastDelay * 1000);
        }
    }
}

import module = require('./module');

module.service('backgroundRefresh', BackgroundRefreshService);

export = BackgroundRefreshService;
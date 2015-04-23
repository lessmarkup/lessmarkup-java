import ng = require('angular');
import _  = require('lodash');

class CommandProcessorService {

    private http: ng.IHttpService;
    private versionId: number;
    private updateProperties: {};
    private backgroundRefresh: BackgroundRefreshService;
    private userSecurity: UserSecurityService;
    private collectionUpdates: CollectionUpdatesService;
    private navigationTree: NavigationTreeService;
    private path: string;
    private rootScope: ng.IRootScopeService;

    public static EVENT_RECEIVED_UPDATES = "commandprocessor.updates";

    constructor(http: ng.IHttpService,
                rootScope: ng.IRootScopeService,
                backgroundRefresh: BackgroundRefreshService,
                userSecurity: UserSecurityService,
                collectionUpdates: CollectionUpdatesService,
                navigationTree: NavigationTreeService,
                initialData: InitialData) {
        this.rootScope = rootScope;
        this.http = http;
        this.versionId = initialData.versionId;
        this.backgroundRefresh = backgroundRefresh;
        this.userSecurity = userSecurity;
        this.collectionUpdates = collectionUpdates;
        this.navigationTree = navigationTree;
    }

    public onPathChanged(path: string) {
        this.path = path;
    }

    private onSuccess(response: ng.IHttpPromiseCallbackArg<ServerResponse>, success: (data: any) => void, failure: (message:string) => void) {
        this.backgroundRefresh.subscribeForUpdates(this.sendIdle);

        this.userSecurity.updateState(response.data.user);

        if (!response.data.success) {
            var message:string = response.data.message || "Error";
            if (failure) {
                failure(message);
            } else {
                console.log(message);
            }
            return null;
        }

        if (response.data.versionId) {
            this.versionId = response.data.versionId;
        }

        if (response.data.collections && response.data.collections.length > 0) {
            this.collectionUpdates.onCollections(response.data.collections);
        }

        if (response.data.topMenu && response.data.topMenu.length > 0) {
            this.navigationTree.onTopMenuChanged(response.data.topMenu);
        }

        if (response.data.navigationTree && response.data.navigationTree.length > 0) {
            this.navigationTree.onNavigationTreeChanged(response.data.navigationTree);
        }

        if (response.data.collectionChanges && response.data.collectionChanges.length > 0) {
            this.collectionUpdates.onCollectionStateChanged(response.data.collectionChanges);
        }

        if (response.data.updates) {
            this.rootScope.$broadcast(CommandProcessorService.EVENT_RECEIVED_UPDATES, response.data.updates);
        }

        if (success) {
            try {
                return success(response.data.data);
            } catch (e) {
                if (failure) {
                    failure(e);
                } else {
                    console.log(e);
                }
            }
        }

        return null;
    }

    private onFailure(response: ng.IHttpPromiseCallbackArg<any>, failure: (message:string) => void) {
        this.backgroundRefresh.subscribeForUpdates(this.sendIdle);
        var message = response.status > 0 ? "Request failed, error " + response.status : "Request failed, unknown communication error";
        if (failure) {
            failure(message);
        } else {
            console.log(message);
        }
    }

    public sendIdle() : void {
        this.sendCommand("idle");
    }

    public sendCommand(command:string, data:any = null, success: (data: any) => void = null, failure: (message:string) => void = null, path:string = null) {

        data = data || {};

        if (command !== "idle") {
            this.backgroundRefresh.onUserActivity(this.sendIdle);
        }

        this.backgroundRefresh.cancelUpdates();

        data["command"] = command;
        if (!path) {
            data["path"] = this.path;
        } else {
            data["path"] = path;
        }

        data["versionId"] = this.versionId;

        _.forIn(this.updateProperties, (value: any, key: string) => {
            if (value != null) {
                data[key] = value;
            }
        }, this);

        return this.http.post("", data).then(
            (response:ng.IHttpPromiseCallbackArg<ServerResponse>) => { this.onSuccess(response, success, failure); },
            (response: ng.IHttpPromiseCallbackArg<any>) => { this.onFailure(response, failure); });
    }
}

import servicesModule = require('./module');
servicesModule.service('commandProcessor', [
    '$http',
    '$rootScope',
    'backgroundRefresh',
    'userSecurity',
    'collectionUpdates',
    'navigationTree',
    'initialData',
    CommandProcessorService]);

import ng = require('angular');
import _  = require('lodash');

import BackgroundRefreshService = require('./BackgroundRefreshService');
import UserSecurityService = require('./userSecurity/UserSecurityService');
import CollectionUpdatesService = require('./collectionUpdates/CollectionUpdatesService');
import NavigationTreeService = require('./NavigationTreeService');

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
    private qService: ng.IQService;

    constructor(http: ng.IHttpService,
                rootScope: ng.IRootScopeService,
                backgroundRefresh: BackgroundRefreshService,
                userSecurity: UserSecurityService,
                collectionUpdates: CollectionUpdatesService,
                navigationTree: NavigationTreeService,
                initialData: InitialData,
                qService: ng.IQService) {
        this.rootScope = rootScope;
        this.http = http;
        this.versionId = initialData.versionId;
        this.backgroundRefresh = backgroundRefresh;
        this.userSecurity = userSecurity;
        this.collectionUpdates = collectionUpdates;
        this.navigationTree = navigationTree;
        this.qService = qService;
    }

    public onPathChanged(path: string) {
        this.path = path;
    }

    private onSuccess<T>(response: ng.IHttpPromiseCallbackArg<ServerResponse<T>>, defer: ng.IDeferred<T>): void {
        this.backgroundRefresh.subscribeForUpdates(this.sendIdle);

        this.userSecurity.updateState(response.data.user);

        if (!response.data.success) {
            var message:string = response.data.message || "Error";
            defer.reject(message);
            return;
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
            this.rootScope.$broadcast(BroadcastEvents.RECORD_UPDATES, response.data.updates);
        }

        try {
            return defer.resolve(response.data.data);
        } catch (e) {
            defer.reject(e);
        }

        return null;
    }

    private onFailure<T>(response: ng.IHttpPromiseCallbackArg<any>, defer: ng.IDeferred<T>) {
        this.backgroundRefresh.subscribeForUpdates(this.sendIdle);
        var message = response.status > 0 ? "Request failed, error " + response.status : "Request failed, unknown communication error";
        defer.reject(message);
    }

    public sendIdle() : void {
        this.sendCommand("idle");
    }

    public onUserActivity():void {
        this.backgroundRefresh.onUserActivity(this.sendIdle);
    }

    public sendCommand<T>(command:string, data:any = null, path:string = null): ng.IPromise<T> {

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

        var defer: ng.IDeferred<T> = this.qService.defer<T>();

        this.http.post<ServerResponse<T>>("", data).then(
            (response:ng.IHttpPromiseCallbackArg<ServerResponse<T>>) => { this.onSuccess(response, defer); },
            (response: ng.IHttpPromiseCallbackArg<any>) => { this.onFailure(response, defer); });

        return defer.promise;
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
    '$q',
    CommandProcessorService]);

export = CommandProcessorService;
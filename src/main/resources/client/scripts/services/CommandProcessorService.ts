import ng = require('angular');
import _  = require('lodash');

import BackgroundRefreshService = require('./BackgroundRefreshService');
import UserSecurityService = require('./userSecurity/UserSecurityService');
import CollectionUpdatesService = require('./collectionUpdates/CollectionUpdatesService');
import NavigationTreeService = require('./NavigationTreeService');
import BroadcastEvents = require('../interfaces/BroadcastEvents');
import ServerResponse = require('../datatypes/ServerResponse');
import NodeConfiguration = require('../datatypes/NodeConfiguration');

class CommandProcessorService {

    private http: ng.IHttpService;
    private versionId: number;
    private updateProperties: {};
    private backgroundRefresh: BackgroundRefreshService;
    private collectionUpdates: CollectionUpdatesService;
    private navigationTree: NavigationTreeService;
    private path: string;
    private rootScope: ng.IRootScopeService;
    private qService: ng.IQService;

    constructor(http: ng.IHttpService,
                rootScope: ng.IRootScopeService,
                backgroundRefresh: BackgroundRefreshService,
                collectionUpdates: CollectionUpdatesService,
                navigationTree: NavigationTreeService,
                initialData: InitialData,
                qService: ng.IQService) {
        this.rootScope = rootScope;
        this.http = http;
        this.versionId = initialData.versionId;
        this.backgroundRefresh = backgroundRefresh;
        this.collectionUpdates = collectionUpdates;
        this.navigationTree = navigationTree;
        this.qService = qService;
        this.path = initialData.path;
        this.rootScope.$on(BroadcastEvents.NODE_LOADED, (event: ng.IAngularEvent, nodeConfiguration: NodeConfiguration) => this.onNodeLoaded(nodeConfiguration));
    }

    private onNodeLoaded(nodeConfiguration: NodeConfiguration) {
        this.path = nodeConfiguration.path;
    }

    private onSuccess<T>(response: ng.IHttpPromiseCallbackArg<ServerResponse<T>>, defer: ng.IDeferred<T>): void {
        this.backgroundRefresh.subscribeForUpdates(() => this.sendIdle());

        this.rootScope.$broadcast(BroadcastEvents.USER_STATE_UPDATE_RECEIVED, response.data.user);

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
        this.backgroundRefresh.subscribeForUpdates(() => this.sendIdle());
        var message = response.status > 0 ? "Request failed, error " + response.status : "Request failed, unknown communication error";
        defer.reject(message);
    }

    public sendIdle() : void {
        this.sendCommand("idle");
    }

    public onUserActivity():void {
        this.backgroundRefresh.onUserActivity(() => this.sendIdle());
    }

    public sendCommand<T>(command:string, data:any = null, path:string = null): ng.IPromise<T> {

        data = data || {};

        if (command !== "idle") {
            this.backgroundRefresh.onUserActivity(() => this.sendIdle());
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

import module = require('./module');

module.service('commandProcessor', [
    '$http',
    '$rootScope',
    'backgroundRefresh',
    'collectionUpdates',
    'navigationTree',
    'initialData',
    '$q',
    CommandProcessorService]);

export = CommandProcessorService;
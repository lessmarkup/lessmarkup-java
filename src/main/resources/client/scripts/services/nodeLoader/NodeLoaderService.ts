import ng = require('angular');
import _ = require('lodash');

import CommandProcessorService = require('../CommandProcessorService');
import BroadcastEvents = require('../../interfaces/BroadcastEvents');
import NodeConfiguration = require('../../datatypes/NodeConfiguration');

class NodeLoaderService {

    private path: string;
    private title: string;
    private commandProcessor: CommandProcessorService;
    private loadingNewPage: boolean = false;
    private rootScope: ng.IRootScopeService;
    private pageProperties: {[name: string]: string } = {};
    private staticNodes: {[name: string]: NodeLoadData } = {};
    private templates: {[name: string]: string } = {};
    private serverConfiguration: ServerConfiguration;
    private qService: ng.IQService;
    private rootPath: string;

    constructor(commandProcessor: CommandProcessorService, rootScope: ng.IRootScopeService, serverConfiguration: ServerConfiguration, browserService: ng.IBrowserService, qService: ng.IQService) {

        this.qService = qService;
        this.serverConfiguration = serverConfiguration;
        this.rootScope = rootScope;
        this.commandProcessor = commandProcessor;

        this.path = '';
        this.initializeBrowser(browserService);
        this.rootPath = window.location['origin'] + this.serverConfiguration.rootPath;
        commandProcessor.onPathChanged(this.path);
        this.resetPageProperties();

        rootScope.$on(BroadcastEvents.USER_LOGGED_OUT, this.onUserLoggedOut);
        rootScope.$on(BroadcastEvents.USER_LOGGED_IN, this.onUserLoggedIn);
    }

    private onUserLoggedIn() {
        this.staticNodes = {};
    }

    private onUserLoggedOut() {
        this.staticNodes = {};
        this.loadNode('/');
    }

    private initializeBrowser(browser: ng.IBrowserService) {
        var browserUrl:string = browser["url"]();
        // dirty hack to prevent AngularJS from reloading the page on pushState and fix $location.$$parse bug
        browser["url"] = () => { return browserUrl; };

        $(window).on('popstate', () => {
            var localPath = location.href;
            if (localPath.substr(0, this.rootPath.length) == this.rootPath) {
                localPath = localPath.substr(this.rootPath.length);
            }
            this.loadNode(localPath+location.search);
            this.rootScope.$apply();
        });
    }

    public getPageProperty(name: string, defaultValue: string = null): string {

        if (this.pageProperties.hasOwnProperty(name)) {
            return this.pageProperties[name];
        }
        return defaultValue;
    }

    public setPageProperty(name: string, value: string): void {

        if (this.getPageProperty(name, null) == value) {
            return;
        }

        this.pageProperties[name] = value;

        this.updatePageHistory();
    }

    private updatePageHistory() : void {

        var query = "";

        _.forIn(this.pageProperties, (value: string, key: string) => {
            if (value === null || value.length == 0) {
                return;
            }

            if (query.length > 0) {
                query += "&";
            }
            query += key + "=" + value;
        }, this);

        var newFullPath = this.rootPath + this.path;

        if (newFullPath[newFullPath.length-1] == '/') {
            newFullPath = newFullPath.substring(0, newFullPath.length-1);
        }

        if (query.length > 0) {
            newFullPath += "?" + query;
        }

        if (window.location.href != newFullPath) {
            history.pushState(newFullPath, this.title, newFullPath);
        }
    }

    private resetPageProperties(currentLink: string = null) {

        this.pageProperties = {};

        if (!currentLink) {
            currentLink = window.location.href;
        }

        var queryPos = currentLink.indexOf('?');

        if (queryPos > 0) {
            var query = currentLink.substring(queryPos + 1, currentLink.length);
            var parameters = query.split('&');
            for (var i = 0; i < parameters.length; i++) {
                if (parameters[i].length == 0) {
                    continue;
                }
                var t = parameters[i].split('=');
                var name = t[0];
                this.pageProperties[name] = t.length > 0 ? t[1] : '';
            }
        }
    }

    public onNodeLoaded(data: NodeLoadData, url: string, deferred: ng.IDeferred<NodeConfiguration> = null): void {

        this.loadingNewPage = false;

        if (url.substring(0, 1) != '/') {
            url = "/" + url;
        }

        this.path = url;
        this.title = data.title;

        this.updatePageHistory();

        var template;
        if (data.template && data.template.length > 0) {
            this.templates[data.templateId] = data.template;
            template = data.template;
        } else {
            template = this.templates[data.templateId];
        }

        if (data.isStatic) {
            this.staticNodes[url] = data;
        }

        //commandHandler.reset();

        if (this.serverConfiguration.useGoogleAnalytics) {
            //ga('send', 'pageview', {
            //    page: '/' + url
            //});
        }

        var finishNodeLoaded = () => {

            var config = new NodeConfiguration();
            config.toolbarButtons = data.toolbarButtons;
            config.viewData = data.viewData;
            config.breadcrumbs = data.breadcrumbs;
            config.title = data.title;
            config.template = template;

            if (deferred != null) {
                deferred.resolve(config);
            }

            this.rootScope.$broadcast(BroadcastEvents.NODE_LOADED, config);

            this.rootScope.$apply();
        };

        if (data.require && data.require.length > 0) {
            require(data.require, function() {
                finishNodeLoaded();
            });
        } else {
            finishNodeLoaded();
        }
    }

    public loadNode(path: string, leaveProperties: boolean = false):ng.IPromise<NodeConfiguration> {

        if (this.loadingNewPage) {
            return this.qService.reject("Already loading new page");
        }

        if (path.substring(0, 1) != '/') {
            path = "/" + path;
        }

        var deferred:ng.IDeferred<NodeConfiguration> = this.qService.defer<NodeConfiguration>();

        this.rootScope.$broadcast(BroadcastEvents.NODE_LOADING, { path: path });

        if (!leaveProperties) {
            this.resetPageProperties(path);
            var queryPos = path.indexOf('?');
            if (queryPos > 0) {
                path = path.substring(0, queryPos);
            }
        }

        if (this.staticNodes.hasOwnProperty(path)) {
            this.onNodeLoaded(this.staticNodes[path], path, deferred);
            return deferred.promise;
        }

        var cachedItems = [];
        for (var key in this.templates) {
            if (!this.templates.hasOwnProperty(key)) {
                continue;
            }
            cachedItems.push(key);
        }

        this.loadingNewPage = true;

        this.commandProcessor.sendCommand<NodeLoadData>("view", {
            cached: cachedItems,
            newPath: path
        }).then((data: NodeLoadData):void => {
            this.loadingNewPage = false;
            this.onNodeLoaded(data, path, deferred);
        }, (message:string):void => {
            this.loadingNewPage = false;
            deferred.reject(message);
        });

        return deferred.promise;
    }

    public getPath(): string {
        return this.path;
    }
}

import module = require('../module');
module.service('nodeLoader', ['commandProcessor', '$rootScope', 'serverConfiguration', '$browser', '$q', NodeLoaderService]);

export = NodeLoaderService;

import ng = require('angular');
import _ = require('lodash');

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

    public static EVENT_LOADING_NEW_NODE = "nodeloader.loadingnewnode";
    public static EVENT_NODE_LOADED = "nodeloader.loaded";

    constructor(commandProcessor: CommandProcessorService, $rootScope: ng.IRootScopeService, serverConfiguration: ServerConfiguration) {
        this.path = "";
        this.rootScope = $rootScope;
        this.serverConfiguration = serverConfiguration;
        commandProcessor.onPathChanged(this.path);
    }

    public getPageProperty(name: string, defaultValue: string = null): string {
        if (this.pageProperties.hasOwnProperty(name)) {
            return this.pageProperties[name];
        }
        return defaultValue;
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

        var newFullPath = window.location.protocol + "//" + window.location.host + this.path;

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

    private onNodeLoaded(data: NodeLoadData, url: string): void {

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
            config.template = data.template;

            this.rootScope.$broadcast(NodeLoaderService.EVENT_NODE_LOADED, config);
        };

        if (data.require && data.require.length > 0) {
            require(data.require, function() {
                finishNodeLoaded();
            });
        } else {
            finishNodeLoaded();
        }
    }

    public loadNode(path: string, leaveProperties: boolean = false, errorFunction: (message: string) => void = null):boolean {
        if (this.loadingNewPage) {
            return false;
        }

        this.rootScope.$broadcast(NodeLoaderService.EVENT_LOADING_NEW_NODE);

        if (!leaveProperties) {
            this.resetPageProperties(path);
            var queryPos = path.indexOf('?');
            if (queryPos > 0) {
                path = path.substring(0, queryPos);
            }
        }

        if (this.staticNodes.hasOwnProperty(path)) {
            this.onNodeLoaded(this.staticNodes[path], path);
            return false;
        }

        var cachedItems = [];
        for (var key in this.templates) {
            if (!this.templates.hasOwnProperty(key)) {
                continue;
            }
            cachedItems.push(key);
        }

        this.loadingNewPage = true;

        this.commandProcessor.sendCommand("view", {
            cached: cachedItems,
            newPath: path
        }, (data):void => {
            this.loadingNewPage = false;
            this.onNodeLoaded(data, path);
        }, (message:string):void => {
            this.loadingNewPage = false;
            if (errorFunction != null) {
                errorFunction(message);
            } else {
                console.log(message);
            }
        });

        return false;
    }

    public getPath(): string {
        return this.path;
    }

    public getFullPath(path: string): string {
        return this.path + "/" + path;
    }

}

import servicesModule = require('./module');
servicesModule.service('nodeLoader', ['commandProcessor', NodeLoaderService]);

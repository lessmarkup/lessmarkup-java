import ng = require('angular');
import _ = require('lodash');

import CommandProcessorService = require('./CommandProcessorService');

class RecordSearchService {

    private timeout: ng.ITimeoutService;
    private searchTimeout: ng.IPromise<any>;
    private commandProcessor: CommandProcessorService;
    private searchResults: SearchResult[];
    private sceService: ng.ISCEService;
    private version: number = 0;

    constructor($timeout: ng.ITimeoutService, commandProcessor: CommandProcessorService, sceService: ng.ISCEService) {
        this.timeout = $timeout;
        this.commandProcessor = commandProcessor;
        this.searchResults = [];
        this.sceService = sceService;
    }

    public addWatch(searchProperty: string, scope: ng.IScope) {
        scope.$watch(searchProperty, function (newValue: string, oldValue: string) {
            if (this.searchTimeout != null) {
                this.timeout.cancel(this.searchTimeout);
            }
            this.searchTimeout = this.timeout(() => {
                this.search(newValue);
            }, 500);
        });
    }

    private searchFinished(response: SearchResponse, searchText: string) {

        this.searchResults = [];

        if (_.isArray(response.results) && response.results.length > 0) {
            this.searchResults = response.results;

            _.forEach(this.searchResults, (searchResult: SearchResult) => {
                searchResult.text = searchResult.text.replace(new RegExp(searchText, "gim"), "<span class=\"highlight\">$&</span>");
                searchResult.text = this.sceService.trustAsHtml(searchResult.text);
            });
        }

        this.version++;
    }

    public getVersion(): number {
        return this.version;
    }

    private search(searchText: string): void {
        this.searchTimeout = null;
        var searchText = searchText.trim();
        if (searchText.length == 0) {
            this.searchResults = [];
            return;
        }
        this.commandProcessor.sendCommand<SearchResponse>("searchText", { text: searchText })
        .then((response: SearchResponse) => {
                this.searchFinished(response, searchText);
            });
    }

    public clearSearch(): void {
        if (this.searchResults.length > 0) {
            this.searchResults = [];
            this.version++;
        }
    }
}

import module = require('./module');
module.service('recordSearch', [
    '$timeout',
    'commandProcessor',
    '$sce',
    RecordSearchService]);

export = RecordSearchService;

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import _ = require('lodash');

class RecordListController {

    private static RECORD_ID_FIELD = "id";

    private configuration:RecordListControllerConfiguration;
    private scope: RecordListControllerScope;
    private records: RecordListRecord[];
    private currentRecordId: number;
    private currentRecord: RecordListRecord;
    private updateProperties: RecordListUpdateProperties;
    private unsubscribeReceivedUpdates;
    private commandProcessor: CommandProcessorService;
    private inputForm: InputFormService;
    private qService: ng.IQService;
    private nodeLoader: NodeLoaderService;

    constructor(
        scope:RecordListControllerScope,
        serverConfiguration: ServerConfiguration,
        commandProcessor: CommandProcessorService,
        inputForm: InputFormService,
        qService: ng.IQService,
        nodeLoader: NodeLoaderService) {

        this.scope = scope;
        this.scope.pageSize = serverConfiguration.pageSize;
        this.configuration = scope.configuration;
        this.updateProperties = new RecordListUpdateProperties();
        this.commandProcessor = commandProcessor;
        this.inputForm = inputForm;
        this.qService = qService;
        this.nodeLoader = nodeLoader;

        scope.configuration = null;

        scope.showOptions = false;
        scope.columns = this.configuration.columns;
        scope.links = this.configuration.links;
        scope.currentPage = "1";
        scope.showPagination = false;
        scope.paginationItems = 7;
        scope.rows = [];
        scope.pageLoaded = false;
        scope.actions = [];
        scope.optionsTemplate = this.configuration.optionsTemplate;
        scope.toolbarButtons = [];
        scope.hasNewRecords = false;
        scope.updating = false;
        scope.hasRecordSearch = this.configuration.hasSearch;
        scope.recordSearchVisible = false;
        scope.recordSearchText = "";
        scope.tableColumns = 0;

        this.records = [];
        this.currentRecordId = null;
        this.currentRecord = null;

        scope.searchRecords = () => { this.onSearchRecords(); };

        scope.toggleRecordSearch = () => { this.toggleRecordSearch(); }

        this.unsubscribeReceivedUpdates = scope.$on(BroadcastEvents.RECORD_UPDATES, (scope, data: ServerResponseUpdates) => {
            this.onUpdatesReceived(data);
        });

        scope.$on('$destroy', () => { this.unsubscribeReceivedUpdates(); });

        scope.refreshNewRecords = () => { this.refreshNewRecords(); };

        this.initializeColumnScopes();
        this.initializeActions();

        scope.hasOptionsBar = scope.columns.length > 0 && (scope.actions.length > 0 || scope.links.length > 0);

        if (scope.hasOptionsBar) {
            scope.columns[0].colSpan = 2;
        }

        scope.onToolbarButtonClick = function (action: RecordListAction) { this.onToolbarButtonClick(action); };

        scope.onClickOptions = function(record: RecordListRecord, column: RecordListColumn, event) => {
            this.onClickOptions(record, column, event);
        };

        scope.isActionVisible = (action: RecordListAction) => {
            if (this.currentRecord === null || !scope.showOptions) {
                return false;
            }
            return action.visible(this.currentRecord);
        };

        scope.sortColumn = (column: RecordListColumn) => { this.sortColumn(column); }

        scope.navigateToLink = (link) => {
            this.nodeLoader.loadNode(this.nodeLoader.getPath() + "/" + this.extractLink(link.url, this.currentRecord));
        };

        scope.linkUrl = (link) => {
            if (this.currentRecord === null) {
                return "";
            }
            return this.nodeLoader.getPath() + "/" + this.extractLink(link.url, this.currentRecord);
        };

        scope.getColumnLink = function (column, row) {
            return this.nodeLoader.getPath() + "/" + this.extractLink(column.url, row);
        };

        this.initializeColumns();

        if (this.configuration.extensionScript && this.configuration.extensionScript.length) {
            require([this.configuration.extensionScript], (extensionScript: (scope: RecordListControllerScope) => void) => {
                extensionScript(this.scope);
                this.onDataReceived(this.configuration);
                this.initializeRecords();
                this.showPage(this.nodeLoader.getPageProperty("p", "1"));
            });
        } else {
            this.onDataReceived(this.configuration);
            this.initializeRecords();
            this.showPage(this.nodeLoader.getPageProperty("p", "1"));
        }
    }

    private onToolbarButtonClick(action: RecordListAction) {
        if (this.scope.updating) {
            return;
        }

        this.commandProcessor.sendCommand(action.name, {newObject: null}).then((record) => {
            this.inputForm.editObject(record, action.type, (record) => {

                var deferred = this.qService.defer<void>();

                this.commandProcessor.sendCommand(action.name, { newObject: record })
                    .then(
                    (data) => { this.handleActionResult(data, null, deferred); },
                    (message: string) => { deferred.reject(message); });

                return deferred.promise;
            });
        });
    }

    private onSearchRecords() {
        if (this.scope.recordSearchText.length > 0) {
            var model = {
                search: this.scope.recordSearchText
            };
            this.updateProperties.filter = JSON.stringify(model);
        } else {
            this.updateProperties.filter = null;
        }
        this.scope.refreshNewRecords();
    }

    private initializeColumnScopes() {
       _.forEach(this.scope.columns, (column: RecordListColumn) => {
            column.colSpan = 1;
            if (!column.scope || !column.scope.length) {
                column.scope = function (obj) { return obj.row; };
            } else {
                column.scope = new Function("obj", "with(obj) { return " + column.scope + "; }");
            }
        });
    }

    private initializeActions() {
        _.forEach(this.configuration.actions, (action: RecordListActionDefinition) => {

            var visibleFunction : Function;

            if (action.visible && action.visible.length > 0) {
                visibleFunction = new Function("obj", "with(obj) { return " + action.visible + "; }");
            } else {
                visibleFunction = function () {
                    return true;
                };
            }

            switch (action.type) {
                case RecordActionType.RECORD:
                case RecordActionType.RECORD_CREATE:
                case RecordActionType.RECORD_INITIALIZE_CREATE:
                    this.scope.actions.push({
                        name: action.name,
                        text: action.text,
                        visible: visibleFunction,
                        type: action.type,
                        parameter: action.parameter
                    });
                    break;
                case RecordActionType.CREATE:
                    this.scope.toolbarButtons.push({
                        name: action.name,
                        text: action.text,
                        visible: visibleFunction,
                        type: action.parameter
                    });
                    break;
            }
        });
    }

    private refreshNewRecords() {
        this.commandProcessor.sendCommand<GetRecordIdsData>("getRecordIds")
            .then((data2: GetRecordIdsData) => {
                this.scope.hasNewRecords = false;

                var savedRecords: {[key: number]: RecordListRecord} = {};

                for (var i = 0; i < this.records.length; i++) {
                    var record = this.records[i];
                    if (record.loaded) {
                        savedRecords[record.id] = record;
                    }
                }

                this.records = [];

                for (var i = 0; i < data2.recordIds.length; i++) {
                    var recordId = data2.recordIds[i];

                    var record;

                    if (savedRecords.hasOwnProperty(recordId)) {
                        record = savedRecords[recordId];
                    } else {
                        record = this.createNewRecord(recordId);
                    }
                    this.records.push(record);
                }

                this.loadVisibleRecords();
            });
    }

    private createNewRecord(recordId: number): RecordListRecord {
        var record: RecordListRecord = new RecordListRecord();
        record.loaded = false;
        for (var i = 0; i < this.scope.columns.length; i++) {
            record[this.scope.columns[i].name] = "";
        }
        record.id = recordId;
        return record;
    }

    private createRecordCopy(record: RecordListRecord): RecordListRecord {
        record.loaded = true;
        return record;
    }

    private toggleRecordSearch() {
        this.scope.recordSearchVisible = !this.scope.recordSearchVisible;

        if (!this.scope.recordSearchVisible) {
            if (this.scope.recordSearchText.length > 0) {
                this.scope.recordSearchText = "";
                this.scope.searchRecords();
            }
        }
    }

    private onUpdatesReceived(updates: ServerResponseUpdates) {
        var hasChanges = false;
        var hasNewRecords = false;
        if (updates.recordsRemoved && updates.recordsRemoved.length > 0) {
            var removedIds = updates.recordsRemoved;
            for (var i = 0; i < removedIds.length; i++) {
                var recordId = removedIds[i];
                for (var j = 0; j < this.records.length; j++) {
                    var record = this.records[j];
                    if (record[RecordListController.RECORD_ID_FIELD] === recordId) {
                        this.records.splice(j, 1);
                        if (this.scope.pageSize > 0) {
                            hasChanges = j < this.scope.pageOffset + this.scope.pageSize;
                        }
                        break;
                    }
                }
            }
        }

        var recordsToUpdate = [];

        if (updates.recordsUpdated && updates.recordsUpdated.length > 0) {
            var updatedIds = updates.recordsUpdated;
            for (var i = 0; i < updatedIds.length; i++) {
                var recordId = updatedIds[i];
                var found = false;
                for (var j = 0; j < this.records.length; j++) {
                    var record = this.records[j];
                    if (record[RecordListController.RECORD_ID_FIELD] === recordId) {
                        if (this.scope.pageSize > 0 && j >= this.scope.pageOffset && j < this.scope.pageOffset + this.scope.pageSize) {
                            recordsToUpdate.push(recordId);
                        } else {
                            this.records[j] = this.createNewRecord(recordId);
                        }
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    hasNewRecords = true;
                }
            }
        }

        if (recordsToUpdate.length > 0) {
            this.updateVisibleRecords(recordsToUpdate, this.scope.currentPage);
        } else if (hasChanges) {
            this.loadVisibleRecords();
        }

        if (!hasNewRecords) {
            return;
        }

        if (this.configuration.manualRefresh) {
            this.scope.hasNewRecords = true;
        }

        this.scope.refreshNewRecords();
    }

    private onDataReceived(data) {
    }


    private resetRecords(recordIds: number[]) {

        this.records = [];

        for (var i = 0; i < recordIds.length; i++) {
            var recordId = recordIds[i];
            var record = this.createNewRecord(recordId);
            this.records.push(record);
        }
    }

    private hideOptions() {
        this.scope.showOptions = false;
    }

    private onClickOptions(record: RecordListRecord, column: RecordListColumn, event) {

        if (this.scope.updating) {
            return;
        }

        if (!this.scope.hasOptionsBar) {
            return;
        }

        if (event && event.target && event.target.nodeName === "A") {
            return;
        }

        if (column !== null && column.ignoreOptions) {
            return;
        }

        var recordId = record.id;

        if (!this.currentRecord || this.currentRecordId !== recordId) {
            this.scope.showOptions = true;
        } else {
            this.scope.showOptions = !this.scope.showOptions;
        }

        this.currentRecordId = recordId;
        this.currentRecord = record;

    }

    private sortColumn(column: RecordListColumn) {

        if (!column.sortable) {
            return;
        }

        if (this.scope.updating) {
            return;
        }

        var current = column.sort;

        for (var i = 0; i < this.scope.columns.length; i++) {
            var column = this.scope.columns[i];
            column.sort = "";
        }

        if (!current || current === "up") {
            current = "down";
        } else {
            current = "up";
        }

        column.sort = current;

        var unloadedRecords = 0;

        for (var i = 0; i < this.records.length; i++) {
            var record = this.records[i];
            if (!record.loaded) {
                unloadedRecords++;
            }
        }

        this.hideOptions();

        if (unloadedRecords !== 0) {
            this.commandProcessor.sendCommand("sort", {column: column.name, direction: current})
                .then((recordIds) => {
                    this.resetRecords(recordIds);
                    this.showPage(1);
                }, (message: string) => {
                    this.inputForm.message(message, "Error");
                });
            return;
        }

        var field = column.name;

        this.records.sort(function (a, b) {
            var af = a[field];
            var bf = b[field];
            if (af === bf) {
                return 0;
            }
            if (af < bf) {
                return -1;
            }
            return 1;
        });

        this.showPage(1);
    }

    private editCurrentRecord(index) {

        this.inputForm.editObject(this.records[index], this.configuration.type, (record) => {
            var deferred: ng.IDeferred<void> = this.qService.defer<void>();

            this.commandProcessor.sendCommand("modifyRecord", { modifiedObject: record })
                .then((data) => {
                    this.handleActionResult(data, index, deferred);
                }, (message: string) => {
                    deferred.reject(message);
            });

            return deferred.promise;
        });
    }

    private handleActionResult(data: any, index: number, deferred: ng.IDeferred<void>) {

        this.onDataReceived(data);

        if (data !== null && data.redirect && data.redirect.length) {
            if (deferred != null) {
                deferred.resolve();
            }

            this.nodeLoader.loadNode(data.redirect);
            return;
        }

        if (data !== null && data.message && data.message.length) {
            if (deferred != null) {
                deferred.resolve();
            }
            this.inputForm.message(data.message, "Information");
            return;
        }

        if (data !== null && data.hasOwnProperty("removed")) {
            this.hideOptions();
            this.records.splice(index, 1);
            this.loadVisibleRecords();
            return;
        }

        if (data !== null && data.hasOwnProperty("reset")) {
            this.records = [];
            this.refreshNewRecords();
            return;
        }

        if (data === null || !data.hasOwnProperty("record")) {
            if (deferred != null) {
                deferred.resolve();
            }
            return;
        }

        var isNew = data.hasOwnProperty("isNew") && data.isNew;
        var newIndex = data.hasOwnProperty("index") ? data.index : -1;

        var record = this.createRecordCopy(data.record);

        if (!isNew && index >= 0 && (newIndex === index || newIndex === -1)) {
            this.records[index] = record;
            this.loadVisibleRecords();
        } else {
            this.hideOptions();
            if (!isNew) {
                this.records.splice(index, 1);
            }
            if (newIndex >= 0) {
                this.records.splice(data.index, 0, record);
            } else {
                newIndex = this.records.length;
                this.records.push(record);
            }
            if (!data.hasOwnProperty("page") || data.page === "last") {
                var totalItems = this.records.length;
                var itemsPerPage = this.scope.pageSize;
                var pageCount = ((totalItems + itemsPerPage - 1) / itemsPerPage) | 0;
                if (pageCount <= 1) {
                    this.loadVisibleRecords();
                } else {
                    var page = (newIndex / itemsPerPage) + 1;
                    this.showPage(page);
                }
            } else {
                this.loadVisibleRecords();
            }
        }

        if (deferred != null) {
            deferred.resolve();
        }
    }

    private executeAction(action: RecordListAction) {

        if (this.currentRecordId === null) {
            return;
        }

        this.commandProcessor.onUserActivity();

        var index;

        for (index = 0; index < this.records.length; index++) {
            var itemId = this.records[index].id;
            if (itemId === this.currentRecordId) {
                break;
            }
        }

        if (index >= this.records.length) {
            return;
        }

        if (action.name === "modifyRecord") {
            this.editCurrentRecord(index);
            return;
        }

        var actionData: any = {
            recordId: this.currentRecordId
        };

        function sendCommand(): ng.IPromise<void> {
            var deferred: ng.IDeferred<void> = this.qService.defer<void>();
            this.commandProcessor.sendCommand(action.name, actionData)
                .then((data) => this.handleActionResult(data, index, deferred));
            return deferred.promise;
        }

        if (action.type === RecordActionType.RECORD_INITIALIZE_CREATE) {
            this.commandProcessor.sendCommand(action.name, actionData)
            .then((data: any) => {
                if (data.message && data.message.length > 0) {
                    this.inputForm.message(data.message, "Information");
                } else {
                    this.inputForm.editObject(data.record, action.parameter, function (object) => {
                        actionData.newObject = object;
                        return sendCommand();
                    });
                }
            }, (message: string) => {
                this.inputForm.message(message, "Error");
            });
        } else if (action.type === RecordActionType.RECORD_CREATE) {
            this.inputForm.editObject(null, action.parameter, (object) => {
                return sendCommand();
            });
        } else {
            sendCommand().catch((message: string) => {
                this.inputForm.message(message, "Error");
            });
        }
    }

    private extractLink(text: string, row: RecordListRow) {
        var link = text;
        var offset = 0;
        while (true) {
            var pos = link.indexOf("{", offset);
            if (pos < 0) {
                break;
            }
            var end = link.indexOf("}", pos);
            if (end < 0) {
                break;
            }

            var parameter = link.substring(pos + 1, end);

            if (!row.hasOwnProperty(parameter)) {
                offset = pos + 1;
                continue;
            }

            parameter = row[parameter];
            link = link.substring(0, pos) + parameter + link.substring(end + 1);
            offset += parameter.length;
        }
        return link;
    }

    private initializeColumns() {

        for (var i = 0; i < this.scope.columns.length; i++) {
            var column = this.scope.columns[i];
            this.scope.tableColumns += column.colSpan;
            if (!column.headerClass) {
                column.headerClass = "";
            }
            if (!column.cellClass) {
                column.cellClass = "grid-data";
            } else {
                column.cellClass += " grid-data";
            }

            switch (column.align) {
                case "Left":
                    column.cellClass += " grid-left";
                    column.headerClass += " grid-left";
                    break;
                case "Center":
                    column.cellClass += " grid-center";
                    column.headerClass += " grid-center";
                    break;
                case "Right":
                    column.cellClass += " grid-right";
                    column.headerClass += " grid-right";
                    break;
            }

            if (column.ignoreOptions) {
                column.cellClass += " ignore-options";
            }

            if (!column.template || column.template.length === 0) {
                var value = "data." + column.name;
                var bind = "ng-bind";
                if (column.allowUnsafe) {
                    value = "getSafeValue(" + value + ")";
                    bind = "ng-bind-html";
                }

                if (column.url && column.url.length > 0) {
                    column.template = "<a href=\"{{getColumnLink(column, row)}}\" ng-click=\"navigateToView(getColumnLink(column, row))\" " + bind + "=\"" + value + "\"></a>";
                } else {
                    column.template = "<span " + bind + "=\"" + value + "\"></span>";
                }
            }
        }

        this.resetRecords(this.configuration.recordIds);

        this.scope.$watch("currentPageNumeric", function () {
            if (!this.scope.pageLoaded) {
                return;
            }

            var itemsPerPage = this.scope.pageSize;
            var pageCount = ((this.scope.totalItems + itemsPerPage - 1) / itemsPerPage) | 0;

            if (this.scope.currentPage !== "last" || this.scope.currentPageNumeric !== pageCount) {
                this.scope.currentPage = this.scope.currentPageNumeric;
            }
        });

        this.loadVisibleRecords();
    }

    private updateVisibleRecords(recordIds, page) {
        this.commandProcessor.sendCommand("fetch", {
            ids: recordIds
        }).then((data) => {
            this.scope.updating = false;
            this.onDataReceived(data);
            this.updateRecords(data.records);
            this.showPage(page);
        }, function (message) {
            this.inputForm.message(message);
            this.scope.updating = false;
        });
    }

    private showPage(page) {

        if (this.scope.updating) {
            return;
        }

        this.scope.totalItems = this.records.length;
        var itemsPerPage = this.scope.pageSize;
        var pageCount = ((this.scope.totalItems + itemsPerPage - 1) / itemsPerPage) | 0;

        var localPage = page;

        if (localPage === "last") {
            localPage = pageCount;
        }

        if (localPage < 1) {
            localPage = 1;
        } else if (localPage > pageCount) {
            localPage = pageCount > 0 ? pageCount : 1;
        }

        var pageOffset = (localPage - 1) * itemsPerPage;
        var pageSize = itemsPerPage;

        if (pageCount === 0) {
            pageSize = 0;
        } else if (pageOffset + pageSize > this.records.length) {
            pageSize = this.records.length - pageOffset;
        }

        var rows: RecordListRecord[] = [];

        for (var i = 0; i < pageSize; i++) {
            var record = this.records[i + pageOffset];
            if (!record.loaded) {
                rows.push(record);
            }
        }

        if (rows.length > 0) {
            this.scope.updating = true;
            this.updateVisibleRecords(rows, page);
            return;
        }

        this.scope.currentPage = page;
        this.scope.currentPageNumeric = localPage;
        this.scope.pageOffset = pageOffset;
        this.scope.pageSize = pageSize;
        this.scope.itemsPerPage = itemsPerPage;

        this.currentRecordId = null;
        this.currentRecord = null;
        this.scope.showOptions = false;

        for (var i = 0; i < this.scope.pageSize; i++) {
            var record = this.records[i + this.scope.pageOffset];
            rows.push(record);
            record.isOdd = (i % 2) !== 0;
        }

        this.scope.showPagination = this.scope.pageSize < this.scope.totalItems;

        this.scope.rows = rows;

        this.scope.pageLoaded = true;

        var pageProperty = page.toString();

        if (pageProperty === "1") {
            pageProperty = "";
        }

        this.nodeLoader.setPageProperty("p", pageProperty);
    }

    private updateRecords(updated: RecordListRecord[]) {
        for (var i = 0; i < updated.length; i++) {
            var source = updated[i];
            var recordId = source.id;
            var target = this.createRecordCopy(source);

            for (var j = 0; j < this.records.length; j++) {
                var currentRecord = this.records[j];
                var currentRecordId = currentRecord.id;
                if (currentRecordId !== recordId) {
                    continue;
                }

                this.records[j] = target;

                for (var k = 0; k < this.scope.rows.length; k++) {
                    var row = this.scope.rows[k];
                    if (row.id === recordId) {
                        this.scope.rows[k] = target;
                        target.isOdd = (k % 2) !== 0;
                        break;
                    }
                }

                break;
            }
        }
    }

    private loadVisibleRecords() {
        this.showPage(this.scope.currentPage);
    }

    private initializeRecords() {
        for (var i = 0; i < this.configuration.records.length; i++) {
            var source = this.configuration.records[i];
            var recordId = source.id;

            for (var j = 0; j < this.configuration.records.length; j++) {
                if (this.records[j].id !== recordId) {
                    continue;
                }
                var target = this.records[j];
                for (var property in source) {
                    target[property] = source[property];
                }

                target.loaded = true;
                break;
            }
        }
    }
}

import module = require('../module');

module.controller('recordList', [
    '$scope',
    'serverConfiguration',
    'commandProcessor',
    'inputForm',
    '$q',
    'nodeLoader',
    RecordListController
]);

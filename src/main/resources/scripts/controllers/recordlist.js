/* 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

app.directive("bindCell", function($compile) {
    return {
        scope: {
            parameters: '=bindCell'
        },
        link: function (scope, element) {

            var innerScope = scope.$parent.$new();

            function updateValue() {
                element.contents().remove();
                innerScope.data = scope.parameters.scope(innerScope);
                var html = $compile(scope.parameters.template)(innerScope);
                element.append(html);
            }

            updateValue();
            scope.$watch("scope.parameters.scope(scope)", updateValue);
        }
    };
});

app.directive("cellShowOptions", function($compile) {

    return {
        replace: false,
        link: function (scope, element) {

            if (!scope.hasOptionsBar || (scope.parameters && scope.parameter.options === false)) {
                return;
            }

            $(element).on("click", function() {
                var row = $(element).parent("tr");

                if (row.hasClass("options-row")) {
                    return true;
                }

                var table = row.closest("table");

                table.find(".options-row").removeClass("options-row");
                table.find(".options-panel").remove();
                table.find(".options-space").remove();

                var $scope = scope.getRecordListScope();

                var space = "<tr class=\"options-space\"><td colspan=\"" + ($scope.columns.length+1).toString() + "\"></td></tr>";

                var html = $compile(scope.optionsTemplate)($scope);

                row.before($(space));
                row.after($(space));

                row.after(html);
                row.addClass("options-row");

                if (!$scope.$$phase) {
                    $scope.$apply();
                }

                return true;
            });
        }
    };
});

app.controller('recordlist', function ($scope, inputForm, $sce) {

    var recordIdField = $scope.viewData.recordId;

    $scope.showOptions = false;
    $scope.columns = $scope.viewData.columns;
    $scope.links = $scope.viewData.links;
    $scope.currentPage = 1;
    $scope.currentPageNumeric = 1;
    $scope.showPagination = false;
    $scope.paginationItems = 7;
    $scope.rows = [];
    $scope.pageLoaded = false;
    $scope.actions = [];
    $scope.optionsTemplate = $scope.viewData.optionsTemplate;
    $scope.toolbarButtons = [];
    $scope.manualRefresh = $scope.viewData.manualRefresh;
    $scope.hasNewRecords = false;
    $scope.updating = false;
    $scope.hasRecordSearch = $scope.viewData.hasSearch;
    $scope.recordSearchVisible = false;
    $scope.recordSearchText = "";
    $scope.tableColumns = 0;

    var records = [];
    var currentRecordId = null;
    var currentRecord = null;

    $scope.searchRecords = function () {
        if ($scope.recordSearchText.length > 0) {
            var model = {
                search: $scope.recordSearchText
            };
            $scope.updateProperties.filter = JSON.stringify(model);
        } else {
            $scope.updateProperties.filter = null;
        }
        $scope.refreshNewRecords();
    };

    $scope.toggleRecordSearch = function() {
        $scope.recordSearchVisible = !$scope.recordSearchVisible;

        if (!$scope.recordSearchVisible) {
            if ($scope.recordSearchText.length > 0) {
                $scope.recordSearchText = "";
                $scope.searchRecords();
            }
        }
    };

    $scope.getSafeValue = function(html) {
        return $sce.trustAsHtml(html);
    };

    $scope.getRecordListScope = function() {
        return $scope;
    };

    var createNewRecord;

    var unsubscribeReceivedUpdates = $scope.$on("receivedUpdates", function(scope, data) {
        var hasChanges = false;
        var hasNewRecords = false;
        if (data.hasOwnProperty("records_removed")) {
            var removedIds = data.records_removed;
            for (var i = 0; i < removedIds.length; i++) {
                var recordId = removedIds[i];
                for (var j = 0; j < records.length; j++) {
                    var record = records[j];
                    if (record[recordIdField] === recordId) {
                        records.splice(j, 1);
                        if ($scope.hasOwnProperty("pageSize") && $scope.pageSize > 0) {
                            hasChanges = j < $scope.pageOffset + $scope.pageSize;
                        }
                        break;
                    }
                }
            }
        }

        var recordsToUpdate = [];

        if (data.hasOwnProperty("records_updated")) {
            var updatedIds = data.records_updated;
            for (var i = 0; i < updatedIds.length; i++) {
                var recordId = updatedIds[i];
                var found = false;
                for (var j = 0; j < records.length; j++) {
                    var record = records[j];
                    if (record[recordIdField] === recordId) {
                        if ($scope.hasOwnProperty("pageSize") && $scope.pageSize > 0 && j >= $scope.pageOffset && j < $scope.pageOffset + $scope.pageSize) {
                            recordsToUpdate.push(recordId);
                        } else {
                            records[j] = createNewRecord(recordId);
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
            updateVisibleRecords(recordsToUpdate, $scope.currentPage);
        } else if (hasChanges) {
            $scope.loadVisibleRecords();
        }

        if (!hasNewRecords) {
            return;
        }

        if ($scope.manualRefresh) {
            $scope.hasNewRecords = true;
            if (!$scope.$$phase) {
                $scope.$apply();
            }
            return;
        }

        $scope.refreshNewRecords();
    });

    $scope.$on('$destroy', function() {
        unsubscribeReceivedUpdates();
    });

    $scope.refreshNewRecords = function() {
        $scope.sendCommand("getRecordIds", null, function (data2) {

            $scope.hasNewRecords = false;

            var savedRecords = {};

            for (var i = 0; i < records.length; i++) {
                var record = records[i];
                if (record.loaded) {
                    savedRecords[record[recordIdField]] = record;
                }
            }

            records = [];

            for (var i = 0; i < data2.recordIds.length; i++) {
                var recordId = data2.recordIds[i];

                var record;

                if (savedRecords.hasOwnProperty(recordId)) {
                    record = savedRecords[recordId];
                } else {
                    record = createNewRecord(recordId);
                }
                records.push(record);
            }

            $scope.loadVisibleRecords();
        });
    };

    function createNewRecord(recordId) {
        var record = { loaded: false };
        for (var i = 0; i < $scope.columns.length; i++) {
            record[$scope.columns[i].name] = "";
        }
        record[recordIdField] = recordId;
        return record;
    }

    function createRecordCopy(record) {
        record.loaded = true;
        return record;
    }

    for (var i = 0; i < $scope.columns.length; i++) {
        var column = $scope.columns[i];
        column.colSpan = 1;
        if (!column.scope || !column.scope.length) {
            column.scope = function(obj) {
                return obj.row;
            };
        } else {
            column.scope = new Function("obj", "with(obj) { return " + column.scope + "; }");
        }
    }

    $scope.onDataReceived = function(scope, data) {
    };

    for (var i = 0; i < $scope.viewData.actions.length; i++) {

        var action = $scope.viewData.actions[i];

        var visibleFunction;

        if (action.visible && action.visible.length > 0) {
            visibleFunction = new Function("obj", "with(obj) { return " + action.visible + "; }");
        } else {
            visibleFunction = function () { return true; };
        }

        switch (action.type) {
            case "RECORD":
            case "RECORD_CREATE":
            case "RECORD_INITIALIZE_CREATE":
                $scope.actions.push({
                    name: action.name,
                    text: action.text,
                    visible: visibleFunction,
                    type: action.type,
                    parameter: action.parameter
                });
                break;
            case "CREATE":
                $scope.toolbarButtons.push({
                    name: action.name,
                    text: action.text,
                    visible: visibleFunction,
                    type: action.parameter
                });
                break;
        }
    }

    $scope.hasOptionsBar = $scope.columns.length > 0 && ($scope.actions.length > 0 || $scope.links.length > 0);

    if ($scope.hasOptionsBar) {
        $scope.columns[0].colSpan = 2;
    }

    $scope.onToolbarButtonClick = function (action) {

        if ($scope.updating) {
            return;
        }

        $scope.sendCommand(action.name, { newObject: null }, function(data) {
            inputForm.editObject($scope, data.record, action.type, function (object, success, failure) {
                $scope.sendCommand(action.name, {
                    newObject: object
                }, function (data) {
                    handleActionResult(data, -1, success, failure);
                }, function (message) {
                    failure(message);
                });
            }, $scope.getTypeahead);
        });
    };

    function resetRecords(recordIds) {

        records = [];

        for (var i = 0; i < recordIds.length; i++) {
            var recordId = recordIds[i];
            var record = createNewRecord(recordId);
            records.push(record);
        }
    }

    $scope.getRecordId = function(record) {
        return record[recordIdField];
    };

    function hideOptions() {
        $scope.showOptions = false;
    }

    $scope.onClickOptions = function(record, column, event) {

        if ($scope.updating) {
            return;
        }

        if (!$scope.hasOptionsBar) {
            return;
        }

        if (event && event.target && event.target.nodeName === "A") {
            return;
        }

        if (column !== null && column.ignoreOptions) {
            return;
        }

        var recordId = record[recordIdField];

        if (!currentRecord || currentRecordId !== recordId) {
            $scope.showOptions = true;
        } else {
            $scope.showOptions = !$scope.showOptions;
        }

        currentRecordId = recordId;
        currentRecord = record;

        if (!$scope.$$phase) {
            $scope.$apply();
        }
    };

    $scope.isActionVisible = function(action) {
        if (currentRecord === null || !$scope.showOptions) {
            return false;
        }
        return action.visible(currentRecord);
    };

    $scope.sortColumn = function(column) {

        if (!column.sortable) {
            return;
        }

        if ($scope.updating) {
            return;
        }

        var current = column.sort;

        for (var i = 0; i < $scope.columns.length; i++) {
            var column = $scope.columns[i];
            column.sort = "";
        }

        if (!current || current === "up") {
            current = "down";
        } else {
            current = "up";
        }

        column.sort = current;

        var unloadedRecords = 0;

        for (var i = 0; i < records.length; i++) {
            var record = records[i];
            if (!record.loaded) {
                unloadedRecords++;
            }
        }

        hideOptions();

        if (unloadedRecords !== 0) {
            $scope.sendCommand("sort", { column: column.name, direction: current }, function (data) {
                resetRecords(data.recordIds);
                $scope.showPage(1);
            }, function(message) {
                inputForm.message(message, "Error");
            });
            return;
        }

        var field = column.name;

        records.sort(function(a, b) {
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

        $scope.showPage(1);
    };

    function editCurrentRecord(index) {

        inputForm.editObject($scope, records[index], $scope.viewData.type, function (object, success, failure) {
            $scope.sendCommand("modifyRecord", {
                modifiedObject: object
            }, function (data) {
                // success
                handleActionResult(data, index, success, failure);
            }, function (message) {
                // failed
                failure(message);
            });
        }, $scope.getTypeahead);
    }

    function handleActionResult(data, index, success) {

        $scope.onDataReceived($scope, data);

        if (data !== null && data.redirect && data.redirect.length) {
            if (success) {
                success();
            }
            $scope.navigateToView(data.redirect);
            return;
        }

        if (data !== null && data.message && data.message.length) {
            if (success) {
                success();
            }
            inputForm.message(data.message, "Information");
            return;
        }

        if (data !== null && data.hasOwnProperty("removed")) {
            hideOptions();
            records.splice(index, 1);
            $scope.loadVisibleRecords();
            return;
        }

        if (data !== null && data.hasOwnProperty("reset")) {
            records = [];
            $scope.refreshNewRecords();
            return;
        }

        if (data === null || !data.hasOwnProperty("record")) {
            if (success) {
                success();
            }
            return;
        }

        var isNew = data.hasOwnProperty("isNew") && data.isNew;
        var newIndex = data.hasOwnProperty("index") ? data.index : -1;

        var record = createRecordCopy(data.record);

        if (!isNew && index >= 0 && (newIndex === index || newIndex === -1)) {
            records[index] = record;
            $scope.loadVisibleRecords();
        } else {
            hideOptions();
            if (!isNew) {
                records.splice(index, 1);
            }
            if (newIndex >= 0) {
                records.splice(data.index, 0, record);
            } else {
                newIndex = records.length;
                records.push(record);
            }
            if (!data.hasOwnProperty("page") || data.page === "last") {
                var totalItems = records.length;
                var itemsPerPage = $scope.viewData.recordsPerPage;
                var pageCount = ((totalItems + itemsPerPage - 1) / itemsPerPage) | 0;
                if (pageCount <= 1) {
                    $scope.loadVisibleRecords();
                } else {
                    var page = (newIndex / itemsPerPage) + 1;
                    $scope.showPage(page);
                }
            } else {
                $scope.loadVisibleRecords();
            }
        }

        if (!$scope.$$phase) {
            $scope.$apply();
        }

        if (success) {
            success();
        }
    }

    $scope.executeAction = function (action) {

        if (currentRecordId === null) {
            return;
        }

        $scope.onUserActivity();

        var index;

        for (index = 0; index < records.length; index++) {
            var itemId = records[index][recordIdField];
            if (itemId === currentRecordId) {
                break;
            }
        }

        if (index >= records.length) {
            return;
        }

        if (action.name === "modifyRecord") {
            editCurrentRecord(index);
            return;
        }

        var actionData = {
            recordId: currentRecordId
        };

        function sendCommand(success, failure) {
            $scope.sendCommand(action.name, actionData, function (data) {
                handleActionResult(data, index, success);
            }, failure);
        }

        if (action.type === "RECORD_INITIALIZE_CREATE") {
            $scope.sendCommand(action.name, actionData, function(data) {
                if (data.message && data.message.length > 0) {
                    inputForm.message(data.message, "Information");
                } else {
                    inputForm.editObject($scope, data.record, action.parameter, function (object, success, failure) {
                        actionData.newObject = object;
                        sendCommand(success, failure);
                    }, $scope.getTypeahead);
                }
            }, function (message) {
                inputForm.message(message, "Error");
            });
        } else if (action.type === "RECORD_CREATE") {
            inputForm.editObject($scope, null, action.parameter, function (object, success, failure) {
                actionData.newObject = object;
                sendCommand(success, failure);
            }, $scope.getTypeahead);
        } else {
            sendCommand(null, function (message) {
                inputForm.message(message, "Error");
            });
        }
    };

    function extractLink(text, row) {
        var link = text;
        var offset = 0;
        while(true) {
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

    $scope.navigateToLink = function(link) {
        $scope.navigateToView($scope.path + "/" + extractLink(link.url, currentRecord));
    };

    $scope.linkUrl = function (link) {
        if (currentRecord === null) {
            return "";
        }
        return $scope.path + "/" + extractLink(link.url, currentRecord);
    };

    $scope.getColumnLink = function(column, row) {
        return $scope.path + "/" + extractLink(column.url, row);
    };

    for (var i = 0; i < $scope.columns.length; i++) {
        var column = $scope.columns[i];
        $scope.tableColumns += column.colSpan;
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

    resetRecords($scope.viewData.recordIds);

    $scope.$watch("currentPageNumeric", function () {
        if (!$scope.pageLoaded) {
            return;
        }

        var itemsPerPage = $scope.viewData.recordsPerPage;
        var pageCount = (($scope.totalItems + itemsPerPage - 1) / itemsPerPage) | 0;

        if ($scope.currentPage !== "last" || $scope.currentPageNumeric !== pageCount) {
            $scope.currentPage = $scope.currentPageNumeric;
        }

        $scope.loadVisibleRecords();
    });

    function updateVisibleRecords(recordIds, page) {
        $scope.sendCommand("fetch", {
            ids: recordIds
        }, function (data) {
            $scope.updating = false;
            $scope.onDataReceived($scope, data);
            $scope.updateRecords(data.records);
            $scope.showPage(page);
        }, function(message) {
            inputForm.message(message);
            $scope.updating = false;
            if (!$scope.$$phase) {
                $scope.$apply();
            }
        });
    }

    $scope.showPage = function (page) {

        if ($scope.updating) {
            return;
        }

        $scope.totalItems = records.length;
        var itemsPerPage = $scope.viewData.recordsPerPage;
        var pageCount = (($scope.totalItems + itemsPerPage - 1) / itemsPerPage) | 0;

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
        } else if (pageOffset + pageSize > records.length) {
            pageSize = records.length - pageOffset;
        }

        var rows = [];

        for (var i = 0; i < pageSize; i++) {
            var record = records[i + pageOffset];
            if (!record.loaded) {
                rows.push(record[recordIdField]);
            }
        }

        if (rows.length > 0) {
            $scope.updating = true;
            updateVisibleRecords(rows, page);
            if (!$scope.$$phase) {
                $scope.$apply();
            }
            return;
        }

        $scope.currentPage = page;
        $scope.currentPageNumeric = localPage;
        $scope.pageOffset = pageOffset;
        $scope.pageSize = pageSize;
        $scope.itemsPerPage = itemsPerPage;

        currentRecordId = null;
        currentRecord = null;
        $scope.showOptions = false;

        for (var i = 0; i < $scope.pageSize; i++) {
            var record = records[i + $scope.pageOffset];

            rows.push(record);

            record.isOdd = (i % 2) !== 0;

        }

        $scope.showPagination = $scope.pageSize < $scope.totalItems;

        $scope.rows = rows;

        if (!$scope.$$phase) {
            $scope.$apply();
        }

        $scope.pageLoaded = true;

        var pageProperty = page.toString();

        if (pageProperty === "1") {
            pageProperty = "";
        }

        $scope.setPageProperty("p", pageProperty);
    };

    $scope.updateRecords = function (updated) {
        for (var i = 0; i < updated.length; i++) {
            var source = updated[i];
            var recordId = source[recordIdField];
            var target = createRecordCopy(source);

            for (var j = 0; j < records.length; j++) {
                var currentRecord = records[j];
                var currentRecordId = currentRecord[recordIdField];
                if (currentRecordId !== recordId) {
                    continue;
                }

                records[j] = target;

                for (var k = 0; k < $scope.rows.length; k++) {
                    var row = $scope.rows[k];
                    if (row[recordIdField] === recordId) {
                        $scope.rows[k] = target;
                        target.isOdd = (k % 2) !== 0;
                        break;
                    }
                }

                break;
            }
        }
    };

    $scope.loadVisibleRecords = function () {
        $scope.showPage($scope.currentPage);
    };

    function initializeRecords() {
        for (var i = 0; i < $scope.viewData.records.length; i++) {
            var source = $scope.viewData.records[i];
            var recordId = source[recordIdField];

            for (var j = 0; j < $scope.viewData.records.length; j++) {
                if (records[j][recordIdField] !== recordId) {
                    continue;
                }
                var target = records[j];
                for (var property in source) {
                    target[property] = source[property];
                }

                target.loaded = true;
                break;
            }
        }
    }

    if ($scope.viewData.extensionScript && $scope.viewData.extensionScript.length) {
        require([$scope.viewData.extensionScript], function(extensionScript) {
            extensionScript($scope);
            $scope.onDataReceived($scope, $scope.viewData);
            initializeRecords();
            $scope.showPage($scope.getPageProperty("p", 1));
        });
    } else {
        $scope.onDataReceived($scope, $scope.viewData);
        initializeRecords();
        $scope.showPage($scope.getPageProperty("p", 1));
    }
});

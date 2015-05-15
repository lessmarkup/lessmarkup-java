/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import RecordListAction = require('./RecordListAction');
import RecordListRecord = require('../../datatypes/RecordListRecord');
import RecordListColumn = require('../../datatypes/RecordListColumn');
import RecordListControllerConfiguration = require('./RecordListControllerConfiguration');

interface RecordListControllerScope extends ng.IScope {
    hasOptionsBar: boolean;
    optionsTemplate: string;
    showOptions: boolean;
    columns: RecordListColumn[];
    links: RecordListLink[];
    rows: RecordListRecord[];
    actions: RecordListAction[];
    configuration: RecordListControllerConfiguration;
    currentPage: string;
    showPagination: boolean;
    paginationItems: number;
    pageLoaded: boolean;
    toolbarButtons: any[];
    hasNewRecords: boolean;
    updating: boolean;
    hasRecordSearch: boolean;
    recordSearchVisible: boolean;
    recordSearchText: string;
    tableColumns: number;
    pageSize: number;
    pageOffset: number;
    totalItems: number;
    itemsPerPage: number;
    currentPageNumeric: number;

    searchRecords: () => void;
    toggleRecordSearch: () => void;
    refreshNewRecords: () => void;
    onToolbarButtonClick: (action: RecordListAction) => void;
    onClickOptions: (record: RecordListRecord, column: RecordListColumn, event) => void;
    isActionVisible: (action: RecordListAction) => boolean;
    sortColumn: (column: RecordListColumn) => void;
    navigateToLink: (link) => void;
    linkUrl: (link) => string;
    getColumnLink: (column, row) => string;
    executeAction: (action: RecordListAction) => void;
}

export = RecordListControllerScope;
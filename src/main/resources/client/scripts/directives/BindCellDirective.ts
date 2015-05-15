/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import _ = require('lodash');

import RecordListRecord = require('../datatypes/RecordListRecord');
import RecordListColumn = require('../datatypes/RecordListColumn');

interface BindCellDirectiveScope extends ng.IScope {
    column: RecordListColumn;
    row: RecordListRecord;
    data: any;
}

class BindCellDirectiveLink {
    private element: JQuery;
    private compileService: ng.ICompileService;
    private scope: BindCellDirectiveScope;

    constructor(scope: BindCellDirectiveScope, element: JQuery, compileService: ng.ICompileService) {
        this.element = element;
        this.scope = scope;
        this.compileService = compileService;

        this.scope.data = this.scope.column.context(this.scope.row);

        scope.$watch(() => this.scope.column.template, (newValue, oldValue) => {
            this.updateValue();
        });
    }

    private updateValue() {
        this.element.contents().remove();
        var html = this.compileService(this.scope.column.template)(this.scope);
        this.element.append(html);
    }
}

import module = require('./module');

module.directive('bindCell', ['$compile', (compileService: ng.ICompileService) => {
    return <ng.IDirective>{
        restrict: 'EA',
        scope: {
            column: '=bindCell',
            row: '='
        },
        link: (scope: BindCellDirectiveScope, element: JQuery) => {
            new BindCellDirectiveLink(scope, element, compileService);
        }
    };
}]);

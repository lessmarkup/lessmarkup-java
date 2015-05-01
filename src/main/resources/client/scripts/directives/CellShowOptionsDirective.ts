/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

class CellShowOptionsDirectiveLink {

    private scope: RecordListControllerScope;
    private compile: ng.ICompileService;

    constructor(scope: RecordListControllerScope, element: JQuery, attrs, compile: ng.ICompileService) {
        if (!scope.hasOptionsBar) {
            return;
        }

        this.scope = scope;
        this.compile = compile;

        element.on("click", () => this.onClickHandler(element) );
    }

    private onClickHandler(element: JQuery): boolean {
        var row = element.parent("tr");

        if (row.hasClass("options-row")) {
            return true;
        }

        this.scope.$apply(() => this.createShowOptions(row) );

        return true;
    }

    private createShowOptions(row: JQuery) {
        var table = row.closest("table");

        table.find(".options-row").removeClass("options-row");
        table.find(".options-panel").remove();
        table.find(".options-space").remove();

        var space = "<tr class=\"options-space\"><td colspan=\"" + (this.scope.columns.length+1).toString() + "\"></td></tr>";

        var html = this.compile(this.scope.optionsTemplate)(this.scope);

        row.before($(space));
        row.after($(space));

        row.after(html);
        row.addClass("options-row");
    }
}

import module = require('./module');

module.directive('cellShowOptions', [() => {
    return <ng.IDirective>{
        restrict: 'A',
        replace: false,
        link: ['$compile', CellShowOptionsDirectiveLink]
    };
}]);

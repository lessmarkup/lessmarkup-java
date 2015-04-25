class CellShowOptionsDirectiveLink {
    constructor(scope: RecordListControllerScope, element: JQuery, attrs, compile: ng.ICompileService) {
        if (!scope.hasOptionsBar) {
            return;
        }

        element.on("click", function() {
            var row = $(element).parent("tr");

            if (row.hasClass("options-row")) {
                return true;
            }

            scope.$apply(() => {
                var table = row.closest("table");

                table.find(".options-row").removeClass("options-row");
                table.find(".options-panel").remove();
                table.find(".options-space").remove();

                var space = "<tr class=\"options-space\"><td colspan=\"" + (scope.columns.length+1).toString() + "\"></td></tr>";

                var html = compile(scope.optionsTemplate)(scope);

                row.before($(space));
                row.after($(space));

                row.after(html);
                row.addClass("options-row");
            });

            return true;
        });
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

import ng = require('angular');

interface ToolbarPanelDirectiveScope extends ng.IScope {
    isButtonEnabled(id: string):boolean;
    onClick(id: string):void;
}

class ToolbarPanelDirective {
    constructor(scope: ToolbarPanelDirectiveScope, eventHandler: EventHandlerService, commandProcessor: CommandProcessorService) {

        scope.isButtonEnabled = (id:string):boolean => {
            return eventHandler.isEnabled(id, this);
        };

        scope.onClick = (id:string):void => {
            commandProcessor.onUserActivity();
            eventHandler.invoke(id, this);
        }
    }
}

import app = require('app');

app.directive('toolbarPanel', [() => {
    return <ng.IDirective>{
        template: '/views/toolbarPanel.html',
        restrict: 'E',
        replace: true,
        scope: true,
        controller: ['$scope', 'eventHandler', ToolbarPanelDirective]
    };
}]);
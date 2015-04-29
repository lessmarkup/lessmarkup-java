interface ToolbarPanelDirectiveScope extends ng.IScope {
    isButtonEnabled(id: string):boolean;
    onClick(id: string):void;
}

import EventHandlerService = require('../services/EventHandlerService');
import CommandProcessorService = require('../services/CommandProcessorService');

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

import module = require('./module');

module.directive('toolbarPanel', ['serverConfiguration', (serverConfiguration: ServerConfiguration) => {
    return <ng.IDirective> {
        template: serverConfiguration.rootPath + '/views/toolbarPanel.html',
        restrict: 'E',
        replace: true,
        scope: true,
        controller: ['$scope', 'eventHandler', ToolbarPanelDirective]
    };
}]);

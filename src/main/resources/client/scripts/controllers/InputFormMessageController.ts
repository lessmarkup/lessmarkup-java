interface InputFormMessageControllerScope extends ng.IScope {
    message: string;
    title: string;
    close: () => void;
}

class InputFormMessageController {
    constructor(scope: InputFormMessageControllerScope,
                dialogService: angular.material.MDDialogService,
                message: string,
                title: string,
                defer: ng.IDeferred<any>) {

        scope.message = message;
        scope.title = title;
        scope.close = () => {
            dialogService.hide();
            defer.resolve();
        };
    }
}

import module = require('./module');

module.controller('inputFormMessage', [
    '$scope',
    '$mdDialog',
    'message',
    'title',
    'defer',
    InputFormMessageController
]);

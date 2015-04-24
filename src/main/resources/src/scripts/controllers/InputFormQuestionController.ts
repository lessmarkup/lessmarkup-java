interface InputFormQuestionControllerScope extends ng.IScope {
    message: string;
    title: string;
    close: () => void;
    submitError: string;
    isApplying: boolean;
}

class InputFormQuestionController {
    constructor(scope: InputFormQuestionControllerScope,
                dialogService: angular.material.MDDialogService,
                message: string,
                title: string,
                defer: ng.IDeferred<any>,
                resolver: () => ng.IPromise<void>) {

        scope.message = message;
        scope.title = title;
        scope.submitError = "";
        scope.isApplying = false;
        scope.close = () => {
            if (resolver == null) {
                dialogService.hide();
                defer.resolve();
            }

            scope.submitError = "";
            scope.isApplying = true;
            var promise: ng.IPromise<void> = resolver();
            promise.then(() => {
                dialogService.hide();
                scope.isApplying = false;
                defer.resolve();
            }, (message: string) => {
                scope.submitError = message;
                scope.isApplying = false;
            });
        };
    }
}

import appControllers = require('app.controllers');

appControllers.controller('inputFormQuestion', [
    '$scope',
    '$mdDialog',
    'message',
    'title',
    'defer',
    'resolver',
    InputFormQuestionController
]);

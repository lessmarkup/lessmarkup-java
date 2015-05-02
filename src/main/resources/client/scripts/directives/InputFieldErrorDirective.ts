import StringFormatter = require('../utilities/StringFormatter');
import _ = require('lodash');

interface InputFieldErrorDirectiveScope {
    maxLengthMessage: string;
    minLengthMessage: string;
    field: InputFieldDefinition;
    form: ng.IFormController;
}

class InputFieldErrorDirectiveController {
    constructor(scope: InputFieldErrorDirectiveScope, translateService: angular.translate.ITranslateService) {
        scope.maxLengthMessage = '';
        if (scope.field.maxLength) {
            translateService('Main.InputFieldMaxLength').then(text => scope.maxLengthMessage = StringFormatter.format(text, scope.field.maxLength));
        }
        scope.minLengthMessage = '';
        if (scope.field.minLength) {
            translateService('Main.InputFieldMinLength').then(text => scope.minLengthMessage = StringFormatter.format(text, scope.field.minLength));
        }
    }
}

import module = require('./module');

module.directive('inputFieldError', [
    'serverConfiguration', (serverConfiguration:ServerConfiguration) => {
        return <ng.IDirective> {
            templateUrl: serverConfiguration.rootPath + '/views/inputFieldError.html',
            restrict: 'E',
            replace: true,
            controller: ['$scope', '$translate', InputFieldErrorDirectiveController]
        };
    }]);

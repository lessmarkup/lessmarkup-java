/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

interface InputFieldDirectiveScope extends ng.IScope {
    field: InputFieldDefinition;
    model: any;
    template: string;
    label: string;
    readOnly: () => string;
}

import InputFieldTypes = require('../interfaces/InputFieldTypes');

class InputFieldDirectiveController {

    private scope:InputFieldDirectiveScope;

    constructor(scope:InputFieldDirectiveScope,
                serverConfiguration:ServerConfiguration) {

        this.scope = scope;

        scope.template = serverConfiguration.rootPath + '/views/inputFields/' + this.getFieldTemplate() + '.html';
        scope.label = scope.field.text;
        scope.readOnly = ():string => {
            if (!scope.field.readOnlyFunction) {
                return scope.field.readOnly ? "readonly" : "";
            }
            return scope.field.readOnlyFunction(scope.model) ? "readonly" : "";
        }
    }

    private getFieldTemplate():string {
        switch (this.scope.field.type) {
            case InputFieldTypes.TEXT:
                return 'text';
            case InputFieldTypes.CODE_TEXT:
                return 'codeText';
            case InputFieldTypes.CHECK_BOX:
                return 'checkBox';
            case InputFieldTypes.DATE:
                return 'date';
            case InputFieldTypes.EMAIL:
                return 'email';
            case InputFieldTypes.FILE:
                return 'file';
            case InputFieldTypes.FILE_LIST:
                return 'fileList';
            case InputFieldTypes.IMAGE:
                return 'image';
            case InputFieldTypes.LABEL:
                return 'label';
            case InputFieldTypes.MULTILINE_TEXT:
                return 'multilineText';
            case InputFieldTypes.MULTI_SELECT:
                return 'multiSelect';
            case InputFieldTypes.NUMBER:
                return 'number';
            case InputFieldTypes.PASSWORD:
                return 'password';
            case InputFieldTypes.PASSWORD_REPEAT:
                return 'passwordRepeat';
            case InputFieldTypes.RICH_TEXT:
                return 'richText';
            case InputFieldTypes.SELECT:
                return 'select';
            case InputFieldTypes.TYPEAHEAD:
                return 'typeahead';
        }
    }
}

import module = require('./module');

module.directive('inputField', [
    'serverConfiguration', (serverConfiguration:ServerConfiguration) => {
        return <ng.IDirective> {
            templateUrl: serverConfiguration.rootPath + '/views/inputField.html',
            restrict: 'E',
            replace: true,
            scope: {
                field: '=',
                model: '='
            },
            controller: ['$scope', 'serverConfiguration', InputFieldDirectiveController]
        };
    }]);


import InputFieldTypes = require('../interfaces/InputFieldTypes');
import _ = require('lodash');
import $ = require('jquery');

function getFieldTemplate(type: String):string {
    switch (type) {
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

interface InputFieldDirectiveScope extends ng.IScope {
    field: InputFieldDefinition;
    object: any;
}

class InputFieldDirectiveLink {

    private scope: InputFieldDirectiveScope;
    private element: ng.IAugmentedJQuery;
    private templateRequest: ng.ITemplateRequestService;
    private compileService: ng.ICompileService;
    private serverConfiguration: ServerConfiguration;

    constructor(scope: InputFieldDirectiveScope, element: ng.IAugmentedJQuery, templateRequest: ng.ITemplateRequestService, compileService: ng.ICompileService, serverConfiguration: ServerConfiguration) {

        this.scope = scope;
        this.element = element;
        this.templateRequest = templateRequest;
        this.compileService = compileService;
        this.serverConfiguration = serverConfiguration;

        scope.$watch('field', () => this.render());
    }

    private getTemplateUrl() {
        return this.serverConfiguration.rootPath + '/views/inputFields/' + getFieldTemplate(this.scope.field.type) + '.html';
    }

    private render() {
        this.templateRequest(this.getTemplateUrl()).then((content: string) => {
            this.onContentLoaded(content);
        });
    }

    private onContentLoaded(content: string) {
        this.element.contents().remove();

        if (!_.isObject(this.scope.field)) {
            return;
        }

        content =  "<md-input-container>" + content + "<input-field-error></input-field-error></md-input-container>";

        var element = $(content).appendTo(this.element);

        this.compileService(element)(this.scope);
    }
}

import module = require('./module');

module.directive('inputField', ['$templateRequest', '$compile', 'serverConfiguration',
        (templateRequest: ng.ITemplateRequestService, compileService: ng.ICompileService, serverConfiguration: ServerConfiguration) => {
    return <ng.IDirective>{
        restrict: 'EA',
        require: ['^form'],
        scope: {
            field: '=',
            object: '=',
            form: '='
        },
        link: (scope: InputFieldDirectiveScope, element: ng.IAugmentedJQuery, attributes: ng.IAttributes) => {
            new InputFieldDirectiveLink(scope, element, templateRequest, compileService, serverConfiguration);
        }
    }}]
);

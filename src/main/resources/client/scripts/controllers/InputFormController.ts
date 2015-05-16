///<amd-dependency path="Recaptcha" />

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import _ = require('lodash');

import InputFormControllerScope = require('./InputFormControllerScope');
import InputFieldTypes = require('../interfaces/InputFieldTypes');

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

class InputFormController {

    private scope: InputFormControllerScope;
    private dialogService: angular.material.MDDialogService;
    private object: any;
    private definition: InputFormDefinition;
    private defer: ng.IDeferred<any>;
    private sceService: ng.ISCEService;
    private serverConfiguration: ServerConfiguration;
    private resolver: (object: any) => ng.IPromise<void>;

    private static PASSWORD_REPEAT_SUFFIX = "$Repeat";

    constructor(scope: InputFormControllerScope,
                dialogService: angular.material.MDDialogService,
                sceService: ng.ISCEService,
                serverConfiguration: ServerConfiguration,
                object: any,
                definition: InputFormDefinition,
                defer: ng.IDeferred<any>,
                resolver: (object: any) => ng.IPromise<void>) {

        this.scope = scope;
        this.dialogService = dialogService;
        this.object = object;
        this.definition = definition;
        this.defer = defer;
        this.sceService = sceService;
        this.serverConfiguration = serverConfiguration;
        this.resolver = resolver;
        this.scope.title = definition.title;

        scope.useCodemirror = _.some(definition.fields, (field: InputFieldDefinition): boolean => field.type == InputFieldTypes.CODE_TEXT);
        scope.submitError = '';
        scope.isApplying = false;
        scope.submitWithCaptcha = definition.submitWithCaptcha;

        scope.okDisabled = (form) => this.okDisabled(form);

        scope.codeMirrorDefaultOptions = InputFormController.getCodeMirrorDefaultOptions();

        scope.isNewObject = object === null;
        scope.object = object != null ? _.cloneDeep(object) : {};

        scope.fieldValueSelected = (field, select) => this.onFieldValueSelected(field, select);

        scope.getValue = (field: InputFieldDefinition) => {
            if (field.type === InputFieldTypes.RICH_TEXT && scope.readOnly(field).length > 0) {
                return sceService.trustAsHtml(object[field.property]);
            }
            return object[field.property];
        };

        scope.getHelpText = (field) => InputFormController.onGetHelpText(field);

        scope.fieldVisible = (field) => {
            if (!field.visibleFunction) {
                return true;
            }
            return field.visibleFunction(scope.object);
        };

        scope.getFieldTemplate = (field) => {
            return serverConfiguration.rootPath + '/views/inputFields/' + getFieldTemplate(field.type) + '.html';
        };

        scope.getTypeahead = (field: InputFieldDefinition, searchText: string): string[] => {
            //if (typeof (getTypeahead) != "function") {
                return [];
            //}
            //return getTypeahead(field, searchText);
        };

        scope.readOnly = (field: InputFieldDefinition): string => {
            if (!field.readOnlyFunction) {
                return field.readOnly ? "readonly" : "";
            }
            return field.readOnlyFunction(scope.object) ? "readonly" : "";
        };

        scope.submit = form => this.onSubmit(form);
        scope.cancel = () => this.onCancel();

        scope.showDateTimeField = (event, field: InputFieldDefinition) => {
            event.preventDefault();
            event.stopPropagation();
            field.isOpen = true;
        };

        this.initializeFields();
    }

    protected okDisabled(form: ng.IFormController): boolean {
        return this.scope.isApplying || (!_.isUndefined(form) && form.$invalid);
    }

    private static onGetHelpText(field: InputFieldDefinition): string {
        var ret = field.helpText;
        if (ret == null) {
            ret = "";
        }
        return ret;
    }

    private static getCodeMirrorDefaultOptions(): any {
        return {
            mode: 'text/html',
            lineNumbers: true,
            lineWrapping: true,
            indentWithTabs: true,
            theme: 'default',
            extraKeys: {
                "F11": (cm: any) => {
                    cm.setOption("fullScreen", !cm.getOption("fullScreen"));
                },
                "Esc": (cm: any) => {
                    if (cm.getOption("fullScreen")) cm.setOption("fullScreen", false);
                }
            }
        };
    }

    private onFieldValueSelected (field: InputFieldDefinition, select: SelectValueDefinition) {
        var value = this.scope.object[field.property];
        switch (field.type) {
            case InputFieldTypes.SELECT:
                return select.value == value;
            case InputFieldTypes.MULTI_SELECT:
                if (!value) {
                    return false;
                }
                for (var i = 0; i < value.length; i++) {
                    if (value[i] == select.value) {
                        return true;
                    }
                }
                return false;
            default:
                return false;
        }
    }

    private processField(field: InputFieldDefinition) {

        switch (field.type) {
            case InputFieldTypes.DYNAMIC_FIELD_LIST:
                this.processDynamicFields(field);
                break;
            case InputFieldTypes.HIDDEN:
                break;
            default:
                this.addField(field);
                break;
        }
    }

    private processDynamicFields(field: InputFieldDefinition) {

        var dynamicFields: DynamicInputFieldDefinition[] = this.scope.object[field.property];

        if (!_.isArray(dynamicFields)) {
            return;
        }

        _.forEach(dynamicFields, (dynamicField: DynamicInputFieldDefinition) => {
            var dynamicDefinition: InputFieldDefinition = _.cloneDeep<InputFieldDefinition>(dynamicField.field);
            dynamicDefinition.property = field.property + "$" + dynamicDefinition.property;
            dynamicDefinition.dynamicSource = dynamicField;
            this.addField(dynamicDefinition);
            this.scope.object[dynamicDefinition.property] = dynamicField.value;
        });
    }

    private addField(field: InputFieldDefinition) {

        if (field.type === InputFieldTypes.PASSWORD_REPEAT) {

            field = _.cloneDeep<InputFieldDefinition>(field);
            field.type = InputFieldTypes.PASSWORD;

            this.addSimpleField(field);

            var passwordRepeat = _.cloneDeep<InputFieldDefinition>(field);
            passwordRepeat.property = field.property + InputFormController.PASSWORD_REPEAT_SUFFIX;
            passwordRepeat.inlineWithPrevious = true;
            passwordRepeat.reference = field.property;
            passwordRepeat.type = InputFieldTypes.PASSWORD_REPEAT;
            passwordRepeat.required = false;

            this.addSimpleField(passwordRepeat);

        } else {
            this.addSimpleField(field);
        }
    }

    private addSimpleField(field: InputFieldDefinition) {

        if (!this.scope.object.hasOwnProperty(field.property)) {
            if (_.isUndefined(field.defaultValue)) {
                this.scope.object[field.property] = "";
            } else {
                this.scope.object[field.property] = field.defaultValue;
            }
        }

        if (!field.inlineWithPrevious || this.scope.fields.length === 0) {
            this.scope.fields.push(field);
        } else {
            var lastField = this.scope.fields[this.scope.fields.length - 1];
            if (lastField.isGroup) {
                lastField.children.push(field);
            } else {
                var groupField: InputFieldDefinition = <any>{};
                groupField.isGroup = true;
                groupField.type = "";
                groupField.children = [];
                groupField.children.push(lastField);
                groupField.children.push(field);
                groupField.position = lastField.position;
                this.scope.fields[this.scope.fields.length-1] = groupField;
            }
        }

        switch (field.type) {
            case InputFieldTypes.IMAGE:
            case InputFieldTypes.FILE:
                this.scope.object[field.property] = null;
                break;
            case InputFieldTypes.SELECT:
                if (_.isArray(field.selectValues)) {
                    var valueDefinitions: SelectValueDefinition[] = <any> field.selectValues; // to hide compilation bug in IntelliJ IDEA
                    if (valueDefinitions.length > 0) {
                        this.scope.object[field.property] = valueDefinitions[0].value;
                    }
                }
                break;
            case InputFieldTypes.DATE:
                field.isOpen = false;
                break;
        }

        if (field.visibleCondition && field.visibleCondition.length > 0) {
            field.visibleFunction = new Function("obj", "with(obj) { return " + field.visibleCondition + "; }");
        } else {
            field.visibleFunction = null;
        }

        if (field.readOnlyCondition && field.readOnlyCondition.length > 0) {
            field.readOnlyFunction = new Function("obj", "with(obj) { return " + field.readOnlyCondition + "; }");
        } else {
            field.readOnlyFunction = null;
        }
    }

    private initializeFields() {
        this.scope.fields = [];
        _.forEach(this.definition.fields, (field: InputFieldDefinition) => {
            this.processField(field);
        });
    }

    private validateField(value: any, field: InputFieldDefinition, formField: ng.INgModelController): void {
        switch (field.type) {
            case InputFieldTypes.FILE:
                if (field.required && this.scope.isNewObject && (value == null || value.file == null || value.file.length == 0)) {
                    formField.$setValidity('required', false);
                }

                if (value.file.length > this.serverConfiguration.maximumFileSize) {
                    formField.$setValidity('maxlength', false);
                }
                return;

            case InputFieldTypes.FILE_LIST:
                if (field.required && this.scope.isNewObject && (value == null || value.length == 0)) {
                    formField.$setValidity('required', false);
                }

                if (value != null) {
                    for (var j = 0; j < value.length; j++) {
                        var file = value[j];
                        if (file.file != null && file.file.length > this.serverConfiguration.maximumFileSize) {
                            formField.$setValidity('maxlength', false);
                            break;
                        }
                    }
                }

                return;
            case InputFieldTypes.MULTI_SELECT:
                return;
        }

        if (typeof (value) == 'undefined' || value == null || value.toString().trim().length == 0) {
            if (field.required) {
                formField.$setValidity('required', false);
            }
            return;
        }

        switch (field.type) {
            case InputFieldTypes.NUMBER:
                if (_.isNaN(parseFloat(value))) {
                    formField.$setValidity('number', false);
                }
                break;
            case InputFieldTypes.EMAIL:
                if (!_.isString(value) || !value.search(/[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}/)) {
                    formField.$setValidity('email', false);
                }
                break;
            case InputFieldTypes.PASSWORD_REPEAT:
                var repeatPassword = this.scope.object[field.property + InputFormController.PASSWORD_REPEAT_SUFFIX];
                if (!_.isString(repeatPassword) || repeatPassword != value) {
                    formField.$setValidity('password_match', false);
                }
        }
    }

    private onCancel() {
        this.dialogService.hide('cancel');
    }

    private onSubmit(form: ng.IFormController) {
        var valid = true;

        for (var i = 0; i < this.scope.fields.length; i++) {
            var field = this.scope.fields[i];

            if (!this.scope.fieldVisible(field) || this.scope.readOnly(field)) {
                continue;
            }

            var value = this.scope.object[field.property];

            this.validateField(value, field, form[field.property]);
        }

        if (form.$invalid) {
            return;
        }

        _.forEach(this.scope.fields, (field: InputFieldDefinition) => {
            if (field.dynamicSource) {
                field.dynamicSource.value = this.scope.object[field.property];
            }
        });

        if (this.scope.submitWithCaptcha) {
            this.scope.object["-RecaptchaChallenge-"] = Recaptcha.get_challenge();
            this.scope.object["-RecaptchaResponse-"] = Recaptcha.get_response();
        }

        this.scope.submitError = "";
        this.scope.isApplying = true;

        try {

            var changed = _.cloneDeep(this.scope.object);

            _.forEach(this.scope.fields, (field: InputFieldDefinition) => {
                if (field.dynamicSource) {
                    delete changed[field.property];
                } else if (field.type == InputFieldTypes.PASSWORD_REPEAT) {
                    delete changed[field.property + InputFormController.PASSWORD_REPEAT_SUFFIX];
                }
            });

            _.forEach(this.definition.fields, (field: InputFieldDefinition) => {
                if (field.type == InputFieldTypes.DYNAMIC_FIELD_LIST) {
                    var dynamicFields: DynamicInputFieldDefinition[] = changed[field.property];
                    if (dynamicFields != null) {
                        for (var j = 0; j < dynamicFields.length; j++) {
                            var dynamicField = dynamicFields[j];
                            var property = dynamicField.field.property;
                            dynamicField.field = <any>{};
                            dynamicField.field.property = property;
                        }
                    }
                }
            });

            if (this.resolver == null) {
                this.dialogService.hide();
                this.defer.resolve(changed);
                return;
            }

            this.resolver(changed).then(() => {
                this.scope.isApplying = false;
                this.dialogService.hide();
                if (this.defer != null) {
                    this.defer.resolve(changed);
                }
            }, (message: string) => {
                this.scope.isApplying = false;
                this.scope.submitError = message;
                if (this.scope.submitWithCaptcha) {
                    Recaptcha.reload();
                }
            });
        } catch (err) {
            this.scope.isApplying = false;
            this.scope.submitError = err.toString();
        }
    }
}

import module = require('./module');

module.controller('inputForm', [
    '$scope',
    '$mdDialog',
    '$sce',
    'serverConfiguration',
    'object',
    'definition',
    'defer',
    'resolver',
    InputFormController
]);

export = InputFormController;

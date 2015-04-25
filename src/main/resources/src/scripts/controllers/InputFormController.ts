///<amd-dependency path="angular.material" />

import Codemirror = require('Codemirror');
import _ = require('lodash');
import Recaptcha = require('recaptcha');

interface InputFormControllerScope extends ng.IScope {
    fields: InputFieldDefinition[];
    submitError: string;
    isApplying: boolean;
    submitWithCaptcha: boolean;
    isDisabled(): boolean;
    codeMirrorDefaultOptions: Codemirror.EditorConfiguration;
    isNewObject: boolean;
    object: any;
    fields: InputFieldDefinition[];
    fieldValueSelected: (field: InputFieldDefinition, select: SelectValueDefinition) => boolean;
    getValue: (field: InputFieldDefinition) => any;
    readOnly: (field: InputFieldDefinition) => string;
    hasErrors: (field: InputFieldDefinition) => boolean;
    getErrorText: (field: InputFieldDefinition) => string;
    getHelpText: (field: InputFieldDefinition) => string;
    fieldVisible: (field: InputFieldDefinition) => boolean;
    getTypeahead: (field: InputFieldDefinition, searchText: string) => string[];
    submit: () => void;
    cancel: () => void;
    showDateTimeField: (event, field: InputFieldDefinition) => void;
}

class InputFormController {

    private scope: InputFormControllerScope;
    private dialogService: angular.material.MDDialogService;
    private object: any;
    private definition: InputFormDefinition;
    private defer: ng.IDeferred<any>;
    private sceService: ng.ISCEService;
    private serverConfiguration: ServerConfiguration;
    private validationErrors: { [key: string]: string };
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
        this.validationErrors = {};
        this.resolver = resolver;

        scope.fields = definition.fields;
        scope.submitError = '';
        scope.isApplying = false;
        scope.submitWithCaptcha = definition.submitWithCaptcha;
        scope.isDisabled = () => {
            return scope.isApplying;
        };
        scope.codeMirrorDefaultOptions = {
            mode: 'text/html',
            lineNumbers: true,
            lineWrapping: true,
            indentWithTabs: true,
            theme: 'default',
            extraKeys: {
                "F11": function (cm) {
                    cm.setOption("fullScreen", !cm.getOption("fullScreen"));
                },
                "Esc": function (cm) {
                    if (cm.getOption("fullScreen")) cm.setOption("fullScreen", false);
                }
            }
        };

        scope.isNewObject = object == null;
        scope.object = object != null ? _.cloneDeep(object) : {};

        scope.fieldValueSelected = (field: InputFieldDefinition, select: SelectValueDefinition) => {
            var value = scope.object[field.property];
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
        };

        scope.getValue = (field: InputFieldDefinition) => {
            if (field.type === InputFieldTypes.RICH_TEXT && scope.readOnly(field).length > 0) {
                return sceService.trustAsHtml(object[field.property]);
            }
            return object[field.property];
        };

        scope.hasErrors = (field: InputFieldDefinition): boolean => {
            return this.validationErrors.hasOwnProperty(field.property);
        };

        scope.getErrorText = (field: InputFieldDefinition): string => {
            return this.validationErrors[field.property];
        };

        scope.getHelpText = (field: InputFieldDefinition): string => {
            var ret = field.helpText;
            if (ret == null) {
                ret = "";
            }
            if (scope.hasErrors(field)) {
                if (ret.length) {
                    ret += " / ";
                }
                ret += scope.getErrorText(field);
            }
            return ret;
        };

        scope.fieldVisible = (field: InputFieldDefinition) => {
            if (!field.visibleFunction) {
                return true;
            }
            return field.visibleFunction(scope.object);
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

        scope.submit = this.onSubmit;
        scope.cancel = this.onCancel;

        scope.showDateTimeField = (event, field: InputFieldDefinition) => {
            event.preventDefault();
            event.stopPropagation();
            field.isOpen = true;
        }

        _.forEach(definition.fields, (field: InputFieldDefinition) => {
            if (!scope.object.hasOwnProperty(field.property)) {
                if (typeof (field.defaultValue) != "undefined") {
                    scope.object[field.property] = field.defaultValue;
                } else {
                    scope.object[field.property] = "";
                }
            }

            if (field.type == InputFieldTypes.DYNAMIC_FIELD_LIST) {

                var dynamicFields: DynamicInputFieldDefinition[] = scope.object[field.property];

                if (dynamicFields == null) {
                    return;
                }

                _.forEach(dynamicFields, (dynamicField: DynamicInputFieldDefinition) => {
                    var dynamicDefinition: InputFieldDefinition = _.cloneDeep<InputFieldDefinition>(dynamicField.field);
                    dynamicDefinition.property = field.property + "$" + dynamicDefinition.property;
                    scope.fields.push(dynamicDefinition);
                    dynamicDefinition.dynamicSource = dynamicField;
                    this.initializeField(dynamicDefinition);
                    scope.object[dynamicDefinition.property] = dynamicField.value;
                });

                for (var j = 0; j < dynamicFields.length; j++) {
                    var dynamicField = dynamicFields[j];
                }
                return;
            }

            if (field.type !== InputFieldTypes.HIDDEN) {
                scope.fields.push(field);
                this.initializeField(field);
            }
        });
    }

    private validateField(value: any, field: InputFieldDefinition): void {
        switch (field.type) {
            case InputFieldTypes.FILE:
                if (field.required && this.scope.isNewObject && (value == null || value.file == null || value.file.length == 0)) {
                    this.validationErrors[field.property] = "Field is required";
                }

                if (value.file.length > this.serverConfiguration.maximumFileSize) {
                    this.validationErrors[field.property] = "File is too big";
                }
                return;

            case InputFieldTypes.FILE_LIST:
                if (field.required && this.scope.isNewObject && (value == null || value.length == 0)) {
                    this.validationErrors[field.property] = "Field is required";
                }

                if (value != null) {
                    for (var j = 0; j < value.length; j++) {
                        var file = value[j];
                        if (file.file != null && file.file.length > this.serverConfiguration.maximumFileSize) {
                            this.validationErrors[field.property] = "File is too big";
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
                this.validationErrors[field.property] = "Field is required";
            }
            return;
        }

        switch (field.type) {
            case InputFieldTypes.NUMBER:
                if (isNaN(parseFloat(value))) {
                    this.validationErrors[field.property] = "Field '" + field.text + "' is not a number";
                }
                break;
            case InputFieldTypes.EMAIL:
                if (!value.search(/[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}/)) {
                    this.validationErrors[field.property] = "Field'" + field.text + "' is not an e-mail";
                }
                break;
            case InputFieldTypes.PASSWORD_REPEAT:
                var repeatPassword = this.scope.object[field.property + InputFormController.PASSWORD_REPEAT_SUFFIX];
                if (typeof (repeatPassword) == 'undefined' || repeatPassword == null || repeatPassword != value) {
                    this.validationErrors[field.property] = 'Passwords must be equal';
                }
        }
    }

    private onCancel() {
        this.dialogService.cancel('cancel');
    }

    private onSubmit() {
        var valid = true;

        this.validationErrors = {};

        for (var i = 0; i < this.scope.fields.length; i++) {
            var field = this.scope.fields[i];

            if (!this.scope.fieldVisible(field) || this.scope.readOnly(field)) {
                continue;
            }

            var value = this.scope.object[field.property];

            this.validateField(value, field);
        }

        if (!_.isEmpty(this.validationErrors)) {
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
                this.defer.resolve(changed);
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

    private initializeField(field: InputFieldDefinition) {
        if (field.type == InputFieldTypes.PASSWORD_REPEAT) {
            this.scope.object[field.property] = "";
            this.scope.object[field.property + InputFormController.PASSWORD_REPEAT_SUFFIX] = "";
        } else if (field.type == InputFieldTypes.IMAGE || field.type == InputFieldTypes.FILE) {
            this.scope.object[field.property] = null;
        }
        if (field.type == InputFieldTypes.SELECT && field.selectValues != null && field.selectValues.length > 0) {
            this.scope.object[field.property] = field.selectValues[0].value;
        }

        if (field.type == InputFieldTypes.DATE) {
            field.isOpen = false;
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

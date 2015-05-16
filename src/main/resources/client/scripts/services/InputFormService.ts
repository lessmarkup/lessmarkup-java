import _ = require('lodash');
import $ = require('jquery');

class FormRequestCommand {
    id: string;
}

import CommandProcessorService = require('./CommandProcessorService');
import ModuleLoaderService = require('./ModuleLoaderService');
import InputFieldTypes = require('../interfaces/InputFieldTypes');

class InputFormService {

    private dialogService: angular.material.MDDialogService;
    private qService: ng.IQService;
    private definitions: {[key: string]: InputFormDefinition} = {};
    private commandProcessor: CommandProcessorService;
    private moduleLoader: ModuleLoaderService;
    private serverConfiguration: ServerConfiguration;

    constructor(rootScope: ng.IRootScopeService,
                qService: ng.IQService,
                dialog: angular.material.MDDialogService,
                commandProcessor: CommandProcessorService,
                moduleLoader: ModuleLoaderService,
                serverConfiguration: ServerConfiguration) {
        this.dialogService = dialog;
        this.qService = qService;
        this.commandProcessor = commandProcessor;
        this.moduleLoader = moduleLoader;
        this.serverConfiguration = serverConfiguration;
    }

    private fillRichTextModules(requires: string[]) {
        requires.push("scripts/directives/CkEditorDirective");
    }

    private fillCodeTextModules(requires: string[]) {
        requires.push("scripts/directives/CodeMirrorDirective");
    }

    private editObjectWithDefinitionAndModulesLoaded<T>(object: T, definition: InputFormDefinition, defer: ng.IDeferred<T>, resolver: (object: T) => ng.IPromise<void>): void {
        this.dialogService.show({
            templateUrl: this.serverConfiguration.rootPath + '/views/inputForm.html',
            locals: {
                object: object,
                definition: definition,
                defer: defer,
                resolver: resolver
            },
            controller: 'inputForm',
            parent: $('#body-wrapper')
        }).catch(() => {
            defer.reject("Cancelled");
        });
    }

    private editObjectWithDefinitionLoaded<T>(object: T, type: string, definition: InputFormDefinition, defer: ng.IDeferred<T>, resolver: (object: T) => ng.IPromise<void>) {
        var hasTinyMce = false;
        var hasCodeMirror = false;
        var hasDynamicFields = false;

        var requires: string[] = [];

        _.forEach<InputFieldDefinition>(definition.fields, (field: InputFieldDefinition) => {
            switch (field.type) {
                case InputFieldTypes.DYNAMIC_FIELD_LIST:
                    hasDynamicFields = true;
                    break;
                case InputFieldTypes.RICH_TEXT:
                    if (!hasTinyMce) {
                        hasTinyMce = true;
                        this.fillRichTextModules(requires);
                    }
                    break;
                case InputFieldTypes.CODE_TEXT:
                    if (!hasCodeMirror) {
                        hasCodeMirror = true;
                        this.fillCodeTextModules(requires);
                    }
                    break;
            }
        });

        if (hasDynamicFields && object == null) {
            defer.reject("Cannot edit uninitialized object");
        }

        if (requires.length > 0) {
            require(requires, () => {
                this.editObjectWithDefinitionAndModulesLoaded(object, definition, defer, resolver);
            });
        } else {
            this.editObjectWithDefinitionAndModulesLoaded(object, definition, defer, resolver);
        }
    }

    public editObject<T>(object: T, type: string, resolver: (object: T) => ng.IPromise<void> = null): ng.IPromise<T> {
        var defer = this.qService.defer<T>();

        this.getDefinition(type).then((definition: InputFormDefinition) => {
            this.editObjectWithDefinitionLoaded(object, type, definition, defer, resolver);
        });

        return defer.promise;
    }

    public question(message: string, title: string, resolver: () => ng.IPromise<void> = null): ng.IPromise<void> {
        var defer = this.qService.defer<void>();

        this.dialogService.show({
            templateUrl: this.serverConfiguration.rootPath + '/views/inputFormQuestion.html',
            locals: {
                message: message,
                title: title,
                defer: defer,
                resolver: resolver
            },
            controller: 'inputFormQuestion'
        }).catch(() => {
            defer.reject();
        });

        return defer.promise;
    }

    public message(message: string, title: string = null): ng.IPromise<void> {

        var defer = this.qService.defer<void>();

        this.dialogService.show({
            templateUrl: this.serverConfiguration.rootPath + '/views/inputFormMessage.html',
            locals: {
                message: message,
                title: title || "LessMarkup",
                defer: defer
            },
            controller: 'inputFormMessage'
        });

        return defer.promise;
    }

    getDefinition(type: string): ng.IPromise<InputFormDefinition> {
        var defer = this.qService.defer<InputFormDefinition>();

        if (this.definitions.hasOwnProperty(type)) {
            defer.resolve(this.definitions[type]);
        } else {
            var request: FormRequestCommand = { id: type };
            this.commandProcessor.sendCommand<InputFormDefinition>('form', request)
                .then((definition: InputFormDefinition) => {
                    this.definitions[type] = definition;
                    defer.resolve(definition);
                }, (data: any) => { defer.reject(data); });
        }

        return defer.promise;
    }
}

import module = require('./module');
module.service('inputForm', [
    '$rootScope',
    '$q',
    '$mdDialog',
    'commandProcessor',
    'moduleLoader',
    'serverConfiguration',
    InputFormService
]);

export = InputFormService;

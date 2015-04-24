import ng = require('angular');
import _ = require('lodash');

class FormRequestCommand {
    id: string;
}

class InputFormService {

    private dialogService: angular.material.MDDialogService;
    private qService: ng.IQService;
    private definitions: {[key: string]: InputFormDefinition} = {};
    private commandProcessor: CommandProcessorService;
    private moduleLoader: ModuleLoaderService;

    constructor(rootScope: ng.IRootScopeService, qService: ng.IQService, dialog: angular.material.MDDialogService, commandProcessor: CommandProcessorService, moduleLoader: ModuleLoaderService) {
        this.dialogService = dialog;
        this.qService = qService;
        this.commandProcessor = commandProcessor;
        this.moduleLoader = moduleLoader;
    }

    private fillRichTextModules(requires: string[]) {
        requires.push("lib/ckeditor/ckeditor");
        requires.push("scripts/directives/angular-ckeditor");
    }

    private fillCodeTextModules(requires: string[]) {
        requires.push("lib/codemirror/codemirror");
        requires.push("lib/codemirror/plugins/css");
        requires.push("lib/codemirror/plugins/css-hint");
        requires.push("lib/codemirror/plugins/dialog");
        requires.push("lib/codemirror/plugins/anyword-hint");
        requires.push("lib/codemirror/plugins/brace-fold");
        requires.push("lib/codemirror/plugins/closebrackets");
        requires.push("lib/codemirror/plugins/closetag");
        requires.push("lib/codemirror/plugins/colorize");
        requires.push("lib/codemirror/plugins/comment");
        requires.push("lib/codemirror/plugins/comment-fold");
        requires.push("lib/codemirror/plugins/continuecomment");
        requires.push("lib/codemirror/plugins/foldcode");
        requires.push("lib/codemirror/plugins/fullscreen");
        requires.push("lib/codemirror/plugins/html-hint");
        requires.push("lib/codemirror/plugins/htmlembedded");
        requires.push("lib/codemirror/plugins/htmlmixed");
        requires.push("lib/codemirror/plugins/indent-fold");
        requires.push("lib/codemirror/plugins/javascript");
        requires.push("lib/codemirror/plugins/javascript-hint");
        requires.push("lib/codemirror/plugins/mark-selection");
        requires.push("lib/codemirror/plugins/markdown-fold");
        requires.push("lib/codemirror/plugins/match-highlighter");
        requires.push("lib/codemirror/plugins/matchbrackets");
        requires.push("lib/codemirror/plugins/matchtags");
        requires.push("lib/codemirror/plugins/placeholder");
        requires.push("lib/codemirror/plugins/rulers");
        requires.push("lib/codemirror/plugins/scrollpastend");
        requires.push("lib/codemirror/plugins/search");
        requires.push("lib/codemirror/plugins/searchcursor");
        requires.push("lib/codemirror/plugins/xml");
        requires.push("lib/codemirror/plugins/xml-fold");
        requires.push("lib/codemirror/plugins/xml-hint");
        requires.push("lib/codemirror/ui-codemirror");
        this.moduleLoader.requireModule('ui.codemirror');
    }

    private editObjectWithDefinitionAndModulesLoaded<T>(object: T, definition: InputFormDefinition, defer: ng.IDeferred<T>, resolver: (object: T) => ng.IPromise<void>): void {
        this.dialogService.show({
            templateUrl: '/views/inputFormTemplate.html',
            locals: {
                object: object,
                definition: definition,
                defer: defer,
                resolver: resolver
            },
            controller: 'inputForm'
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
                this.moduleLoader.loadModules();
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
            templateUrl: '/views/inputFormQuestionTemplate.html',
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

    public message(message: string, title: string): ng.IPromise<void> {

        var defer = this.qService.defer<void>();

        this.dialogService.show({
            templateUrl: '/views/inputFormMessageTemplate.html',
            locals: {
                message: message,
                title: title,
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

class InputFormServiceProvider implements ng.IServiceProvider {
    $get() {
        return ['$rootScope', '$q', '$mdDialog', 'commandProcessor', 'moduleLoader', InputFormService];
    }
}

import app = require('app');
app.provider('inputForm', InputFormServiceProvider);

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import _ = require('lodash');

interface CkEditorEditor {
    getData(noEvents?: Object): string;
    setData(data: string, options?: { internal?: boolean; callback?: Function; noSnapshot?: boolean; }): void;
    on(eventName: string, listenerFunction: (eventInfo: CkEditorEventInfo) => void, scopeObj?: Object, listenerData?: Object, priority?: number): Object;
}

interface CkEditorEventInfo {
    data: any;
    editor: CkEditorEditor;
    listenerData: any;
    name: string;
    sender: any;
    cancel(): void;
    removeListener(): void;
    stop(): void;
}

interface CkEditor {
    replace(element: string, config?: any): CkEditorEditor;
    replace(element: HTMLTextAreaElement, config?: any): CkEditorEditor;
    appendTo(element: string, config?: any): CkEditorEditor;
    appendTo(element: HTMLTextAreaElement, config?: any): CkEditorEditor;
}

interface CkEditorDirectiveScope extends ng.IScope {
    options: any;
    readonly: boolean;
}

class CkEditorOptions {
    smiley_descriptions: string[];
    smiley_images: string[];
    smiley_path: string;
}

import FriendlyFormattingService = require('../services/FriendlyFormattingService');

class CkEditorDirectiveLink {

    private smileIdToCode: {[id: string]: string} = {};
    private smileCodeToId: {[code: string]: string} = {};

    private serverConfiguration: ServerConfiguration;
    private smileyPath: string;
    private options: CkEditorOptions;
    private scope: CkEditorDirectiveScope;
    private friendlyFormatting: FriendlyFormattingService;
    private editor: CkEditorEditor;
    private model: ng.INgModelController;
    private inputContainer: IInputContaner;

    constructor(scope: CkEditorDirectiveScope,
                element: JQuery,
                attrs,
                model: ng.INgModelController,
                inputContainer: IInputContaner,
                serverConfiguration: ServerConfiguration,
                friendlyFormatting: FriendlyFormattingService,
                ckeditor: CkEditor) {
        this.serverConfiguration = serverConfiguration;
        this.options = new CkEditorOptions();
        this.scope = scope;
        this.friendlyFormatting = friendlyFormatting;
        this.model = model;
        this.inputContainer = inputContainer;
        this.initializeSmiles();

        inputContainer.setHasValue(true);

        this.editor = ckeditor.appendTo(<any>element[0], this.options);

        this.editor.on('change', () => this.applyChanges());
        this.editor.on('key', () => this.applyChanges());

        model.$render = () => this.render();
    }

    private updateEditorState(newValue: any) {
        this.inputContainer.setHasValue(true);
        this.inputContainer.setInvalid(this.model.$invalid && this.model.$touched);
    }

    private render() {
        var text = this.model.$viewValue || '';

        if (text && this.friendlyFormatting.getSmilesExpression()) {
            text = this.friendlyFormatting.smilesToImg(text);
        }

        this.editor.setData(text);
        this.updateEditorState(text);
    }

    private applyChanges() {
        this.scope.$apply(() => this.setViewValue() );
    }

    private setViewValue() {
        var text = this.editor.getData();

        if (this.friendlyFormatting.getSmilesExpression() != null) {
            text = text.replace(/<img\s+alt="([^"]*)"\s+src="[^"]*"\s+(?:style="[^"]*"\s+)?title="([^"]*)"\s+\/?>/gi, (match, alt, title) => {

                if (!alt || !title || alt != title) {
                    return match;
                }

                if (!this.smileCodeToId.hasOwnProperty(alt)) {
                    return match;
                }
                return alt;
            });
        }

        this.model.$setViewValue(text);
        this.model.$setTouched();
        this.updateEditorState(text);
    }

    private initializeSmiles() {
        if (!this.serverConfiguration.smilesBase || this.serverConfiguration.smilesBase.length === 0 || !this.serverConfiguration.smiles || this.serverConfiguration.smiles.length === 0) {
            return;
        }

        this.options.smiley_path = this.serverConfiguration.smilesBase;
        this.options.smiley_descriptions = [];
        this.options.smiley_images = [];

        _.forEach(this.serverConfiguration.smiles, (smile: Smile) => {
            this.smileCodeToId[smile.code] = smile.id;
            this.smileIdToCode[smile.id] = smile.code;
            this.options.smiley_descriptions.push(smile.code);
            this.options.smiley_images.push(smile.id);
        });
    }
}

import module = require('./module');

module.directive('ckEditor', ['serverConfiguration', 'friendlyFormatting', (serverConfiguration: ServerConfiguration, friendlyFormatting: FriendlyFormattingService) => {
    return <ng.IDirective>{
        require: ['?ngModel', '^?mdInputContainer'],
        restrict: 'AE',
        replace: false,
        scope: {
            options: '=',
            readonly: '='
        },
        link: (scope: CkEditorDirectiveScope, element: JQuery, attrs, controllers: any[]) => {
            require(['ckeditor'], (ckeditor) => {
                return new CkEditorDirectiveLink(scope, element, attrs, controllers[0], controllers[1], serverConfiguration, friendlyFormatting, ckeditor);
            });
        }
    };
}]);

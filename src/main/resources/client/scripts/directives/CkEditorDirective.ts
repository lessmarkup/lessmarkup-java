/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

///<amd-dependency path="ckeditor" />

import _ = require('lodash');
//import CKEDITOR = require('ckeditor');

interface CkEditorDirectiveScope extends ng.IScope {

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
    private ck: any;
    private ngModel: ng.INgModelController;

    constructor(scope: CkEditorDirectiveScope, element: JQuery, attrs, ngModel: ng.INgModelController, serverConfiguration: ServerConfiguration, friendlyFormatting: FriendlyFormattingService) {
        this.serverConfiguration = serverConfiguration;
        this.options = new CkEditorOptions();
        this.scope = scope;
        this.friendlyFormatting = friendlyFormatting;
        this.ngModel = ngModel;
        this.initializeSmiles();

        this.ck = CKEDITOR.replace(<any>element[0], this.options);

        this.ck.on('change', () => this.applyChanges);
        this.ck.on('key', () => this.applyChanges);

        ngModel.$render = () => this.render();
    }

    private render() {
        var text = this.ngModel.$modelValue;

        if (text && this.friendlyFormatting.getSmilesExpression()) {
            text = this.friendlyFormatting.smilesToImg(text);
        }

        this.ck.setData(text);
    }

    private applyChanges() {
        this.scope.$apply(() => this.setViewValue() );
    }

    private setViewValue() {
        var text = this.ck.getData();

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

        this.ngModel.$setViewValue(text);
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

module.directive('ckEditor', [() => {
    return <ng.IDirective>{
        require: '?ngModel',
        restrict: 'A',
        replace: false,
        link: ['serverConfiguration', 'friendlyFormatting', CkEditorDirectiveLink]
    };
}]);

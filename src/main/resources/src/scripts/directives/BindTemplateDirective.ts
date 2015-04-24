/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import ng = require('angular');
import $ = require('jQuery');

interface BindTemplateDirectiveScope extends ng.IScope {
    template: string;
    configuration: any;
}

interface BindTemplateDirectiveInnerScope extends ng.IScope {
    configuration: any;
}

class BindTemplateDirectiveLink {

    private scope: BindTemplateDirectiveScope;
    private compileService: ng.ICompileService;
    private innerScope: BindTemplateDirectiveInnerScope;
    private element: JQuery;
    private moduleLoader: ModuleLoaderService;

    constructor(scope: BindTemplateDirectiveScope, element: JQuery, attrs, compileService: ng.ICompileService, moduleLoader: ModuleLoaderService) {
        this.scope = scope;
        this.compileService = compileService;
        this.innerScope = <BindTemplateDirectiveInnerScope> scope.$new(true);
        this.element = element;
        this.moduleLoader = moduleLoader;

        scope.$watch('template', (newValue: string) => {
            this.onTemplateChanged(newValue);
        });
    }

    private onTemplateChanged(newValue: string) {
        this.element.contents().remove();

        if (newValue && newValue.length > 0) {
            this.innerScope.configuration = this.scope.configuration;
            this.moduleLoader.loadModules();
            var compiled: ng.ITemplateLinkingFunction = this.compileService(newValue);
            var newElement: JQuery = compiled(this.innerScope);
            this.element.append(newElement);
        }
    }
}

import appDirectives = require('app.directives');

appDirectives.directive('bindTemplate', [() => {
    return <ng.IDirective>{
        template: '<div></div>',
        restrict: 'E',
        replace: false,
        scope: {
            template: '=',
            configuration: '='
        },
        controller: ['$compile', 'moduleLoader', BindTemplateDirectiveLink]
    };
}]);

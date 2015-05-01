/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

interface BindModuleDirectiveScope extends ng.IScope {
    template: string;
    configuration: any;
}

interface BindModuleDirectiveInnerScope extends ng.IScope {
    configuration: any;
}

import ModuleLoaderService = require('../services/ModuleLoaderService');

class BindModuleDirectiveLink {

    private scope: BindModuleDirectiveScope;
    private compileService: ng.ICompileService;
    private innerScope: BindModuleDirectiveInnerScope;
    private element: JQuery;
    private moduleLoader: ModuleLoaderService;

    constructor(scope: BindModuleDirectiveScope, element: JQuery, compileService: ng.ICompileService, moduleLoader: ModuleLoaderService) {
        this.scope = scope;
        this.compileService = compileService;
        this.innerScope = <BindModuleDirectiveInnerScope> scope.$new(true);
        this.element = element;
        this.moduleLoader = moduleLoader;

        scope.$watch('template', (newValue: string) => this.onTemplateChanged(newValue) );
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

import module = require('./module');

module.directive('bindModule', [
    '$compile',
    'moduleLoader',
    (compileService: ng.ICompileService, moduleLoader: ModuleLoaderService) => {
        return <ng.IDirective>{
            template: '<div></div>',
            restrict: 'E',
            replace: false,
            scope: {
                template: '=',
                configuration: '='
            },
            link: (scope: BindModuleDirectiveScope, element: JQuery) => {
                new BindModuleDirectiveLink(scope, element, compileService, moduleLoader);
            }
        };
    }
]);

///<amd-dependency path="angular.material" />
///<amd-dependency path="controllers/index" />
///<amd-dependency path="directives/index" />
///<amd-dependency path="services/index" />
///<amd-dependency path="angular.touch" />
///<amd-dependency path="angular.translate" />

import ng = require('angular');
import ngRoute = require('angular.route');
import document = require('domready');
import _ = require('lodash');

interface IApplication extends ng.IModule {
    initialize: (initialData: InitialData, serverConfiguration: ServerConfiguration, languages: Language[]) => void;
}

var appModule = <IApplication> ng.module('app', [
    'angular.material',
    'angular.touch',
    'pascalprecht.translate',
    'app.controllers',
    'app.directives',
    'app.services'
]);

appModule.initialize = (initialData: InitialData, serverConfiguration: ServerConfiguration, languages: Language[]): void => {

    app.value('initialData', initialData);
    app.value('serverConfiguration', serverConfiguration);
    app.value('languages', languages);

    appModule.config([
        '$controllerProvider',
        '$compileProvider',
        '$routeProvider',
        '$filterProvider',
        '$provide',
        '$translateProvider',
        'moduleLoader',
        (controllerProvider: ng.IControllerProvider,
         compileProvider: ng.ICompileProvider,
         filterProvider: ng.IFilterProvider,
         provide: ng.auto.IProvideService,
         translateProvider: angular.translate.ITranslateProvider,
         moduleLoader: ModuleLoaderService) => {

            _.forEach(languages, (language: Language) => {
                translateProvider.translations(language.id, language.translations);

                if (language.isDefault) {
                    language.selected = true;
                    translateProvider.preferredLanguage(language.id);
                }

                language.translations = null;
            });

            moduleLoader.initialize(appModule, controllerProvider, compileProvider, filterProvider, provide);

        }]);

    ng.bootstrap(document, ['app']);
};

export = appModule;

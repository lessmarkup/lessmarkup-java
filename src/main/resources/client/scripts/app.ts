///<amd-dependency path="angular.material" />
///<amd-dependency path="controllers/index" />
///<amd-dependency path="directives/index" />
///<amd-dependency path="services/index" />
///<amd-dependency path="angular.touch" />
///<amd-dependency path="angular.translate" />
///<amd-dependency path="./services/ModuleLoaderServiceProvider" />

import ng = require('angular');
import document = require('domready');
import _ = require('lodash');

import ModuleLoaderServiceProvider = require('./services/ModuleLoaderServiceProvider');

var appModule = <IApplication> ng.module('app', [
    'ngMaterial',
    'ngTouch',
    'ngMessages',
    'pascalprecht.translate',
    'app.controllers',
    'app.directives',
    'app.services'
]);

appModule.initialize = (initialData: InitialData, serverConfiguration: ServerConfiguration, languages: Language[]): void => {

    appModule.value('initialData', initialData);
    appModule.value('serverConfiguration', serverConfiguration);
    appModule.value('languages', languages);

    appModule.config([
        '$translateProvider',
        'moduleLoaderProvider',
        (translateProvider: angular.translate.ITranslateProvider,
         moduleLoaderProvider: ModuleLoaderServiceProvider) => {

            var defaultLanguage: Language = null;

            _.forEach(languages, (language: Language) => {
                translateProvider.translations(language.shortName, language.translations);

                if (language.isDefault) {
                    language.selected = true;
                    defaultLanguage = language;
                    translateProvider.preferredLanguage(language.shortName);
                }

                language.translations = null;
            });

            if (defaultLanguage == null && languages.length > 0) {
                translateProvider.preferredLanguage(languages[0].shortName);
            }

            moduleLoaderProvider.initialize(appModule);

        }]);

    ng.bootstrap(<any>document, ['app']);
};

var module:ng.IModule = appModule;

export = module;

import _ = require('lodash');

class LanguageInformation {
    id: string;
    imageUrl: string;
    shortName: string;
    name: string;
}

interface LanguagesPanelDirectiveScope extends ng.IScope {
    switchLanguage: (id: string) => void;
    languages: LanguageInformation[];
    selectedLanguage: LanguageInformation;
}

class LanguagesPanelDirectiveController {

    private languages: {[id: string]: LanguageInformation} = {};

    constructor(scope: LanguagesPanelDirectiveScope, languages: Language[]) {
        scope.languages = [];

        _.forEach(languages, (src: Language) => {
            var tgt: LanguageInformation = {
                id: src.id,
                imageUrl: src.iconUrl,
                shortName: src.shortName,
                name: src.name
            };

            scope.languages.push(tgt);

            if (src.selected) {
                scope.selectedLanguage = tgt;
            }

            this.languages[tgt.id] = tgt;
        });

        if (scope.selectedLanguage == null && scope.languages.length > 0) {
            scope.selectedLanguage = scope.languages[0];
        }

        scope.switchLanguage = (id: string) => {
            scope.selectedLanguage = this.languages[id];
        }
    }
}

import module = require('./module');

module.directive('languagesPanel', ['serverConfiguration', (serverConfiguration: ServerConfiguration) => {
    return <ng.IDirective>{
        templateUrl: serverConfiguration.rootPath + '/views/languagesPanel.html',
        restrict: 'E',
        replace: true,
        scope: true,
        controller: ['$scope', 'languages', LanguagesPanelDirectiveController]
    };
}]);

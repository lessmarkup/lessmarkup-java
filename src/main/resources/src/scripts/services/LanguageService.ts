class LanguageService {
    private selectedLanguage: Language;
    private languages: Language[];

    constructor(serverConfiguration: ServerConfiguration) {

        this.languages = serverConfiguration.languages;
        this.selectedLanguage = null;
        for (var i = 0; i < this.languages.length; i++) {
            if (this.languages[i].selected) {
                this.selectedLanguage = this.languages[i];
                break;
            }
        }
    }

    public getSelectedLanguage(): Language {
        return this.selectedLanguage;
    }
}

import servicesModule = require('./module');
servicesModule.service('language', [
    'serverConfiguration',
    LanguageService]);

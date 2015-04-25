class LanguageService {
    private selectedLanguage: Language;
    private languages: Language[];

    constructor(languages: Language[]) {

        this.languages = languages;
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
    'languages',
    LanguageService]);

export = LanguageService;
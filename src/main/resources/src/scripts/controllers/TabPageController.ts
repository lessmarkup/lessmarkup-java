interface TabPagePage {

}

interface TabPageControllerConfiguration {
    pages: TabPagePage[];
}

interface TabPageControllerScope extends ng.IScope {
    configuration: TabPageControllerConfiguration;
    pages: TabPagePage[];
    activePage: TabPagePage;
}

class TabPageController {

    private scope: TabPageControllerScope;

    constructor(scope: TabPageControllerScope) {
        this.scope = scope;
    }

    private loadPages() {
        this.scope.pages = this.scope.configuration.pages;
        this.scope.activePage = this.scope.pages.length > 0 ? this.scope.pages[0] : null;

        for (var i = 0; i < this.scope.pages.length; i++) {
            var page = this.scope.pages[i];
        }
    }
}

import module = require('./module');
module.controller("tabPage", ['$scope', TabPageController]);

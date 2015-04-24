import _ = require('lodash');

class NavigationTreeService {

    private navigationTree: MenuItem[];
    private topMenu: TopMenuItem[];
    private version: number = 0;

    constructor(initialData: InitialData) {
        this.topMenu = initialData.topMenu;
        this.navigationTree = initialData.navigationTree;
        this.updateNavigationTree();
    }

    public onTopMenuChanged(topMenu: TopMenuItem[]):void {
        this.topMenu = topMenu;
        this.version++;
    }

    private updateNavigationTree() {
        _.forEach(this.navigationTree, (menuItem: MenuItem) => {
            menuItem.style = 'margin-left:' + (menuItem.level).toString() + 'em;';
        });
    }

    public onNavigationTreeChanged(navigationTree: MenuItem[]):void {
        this.navigationTree = navigationTree;
        this.updateNavigationTree();
        this.version++;
    }

    public getVersion(): number {
        return this.version;
    }
}

import servicesModule = require('./module');
servicesModule.service('navigationTree', [
    'initialData',
    NavigationTreeService]);

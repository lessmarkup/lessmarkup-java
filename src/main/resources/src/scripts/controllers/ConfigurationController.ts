/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

interface ConfigurationGroup {
    title: string;
    items: ConfigurationNode[];
}

interface ConfigurationNode {
    title: string;
    path: string;
}

interface ConfigurationControllerInitialData {
    groups: ConfigurationGroup[];
}

interface ConfigurationControllerScope extends ng.IScope {
    configuration: ConfigurationControllerInitialData;
    groups: ConfigurationGroup[];
}

class ConfigurationController {
    constructor(scope: ConfigurationControllerScope) {
        scope.groups = scope.configuration.groups;
    }
}

import module = require('./module');
module.controller("configuration", ['$scope', ConfigurationController]);

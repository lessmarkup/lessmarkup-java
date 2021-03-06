/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

interface FileReadDirectiveScope extends ng.IScope {
    value: FileReadResponse;
    clear: () => void;
    chooseFile: () => void;
    comment: string;
}

class FileReadDirectiveLink {

    private scope: FileReadDirectiveScope;

    constructor(scope: FileReadDirectiveScope, element: JQuery) {

        this.scope = scope;

        var fileElement = element.find('file');

        scope.chooseFile = () => fileElement.click();

        scope.clear = () => {
            fileElement.val('');
            fileElement.html('');
            scope.value = null;
        };

        fileElement.bind("change", (changeEvent) => this.onFileSelected(changeEvent) );
    }

    private onFileSelected(changeEvent) {
        this.scope.comment = changeEvent.target.value[0].name;
        var reader = new FileReader();
        reader.onload = (loadEvent) => {
            this.scope.$apply(() => this.onReadFinished(loadEvent, changeEvent));
        };
        reader.readAsDataURL(changeEvent.target.value[0]);
    }

    private onReadFinished(loadEvent, changeEvent) {
        var value = loadEvent.target.result;

        var pos = value.indexOf("base64,");
        if (pos > 0) {
            value = value.substring(pos + 7);
        }

        this.scope.value = {
            file: value,
            type: changeEvent.target.value[0].type,
            name: changeEvent.target.value[0].name,
            id: null
        };
    }
}

import module = require('./module');

module.directive('fileRead', ['serverConfiguration', (serverConfiguration: ServerConfiguration) => {
    return <ng.IDirective>{
        templateUrl: serverConfiguration.rootPath + '/views/fileRead.html',
        restrict: 'E',
        replace: false,
        scope: {
            value: '='
        },
        link: FileReadDirectiveLink
    };
}]);

///<amd-dependency path="../datatypes/FileReadResponse" />

class MultiFileReadResponse {
    files: FileReadResponse[];
}

interface MultiFileReadDirectiveScope extends ng.IScope {
    value: MultiFileReadResponse;
    removeFile: (file: FileReadResponse) => void;
    chooseFile: () => void;
}

class MultiFileReadDirectiveLink {

    private scope: MultiFileReadDirectiveScope;
    private fileId: number = 1;
    private fileReadBefore: number;
    private fileElement: JQuery;

    constructor(scope: MultiFileReadDirectiveScope, element: JQuery) {

        this.scope = scope;
        if (scope.value == null) {
            scope.value = new MultiFileReadResponse();
            this.fileReadBefore = 0;
        } else {
            this.fileReadBefore = scope.value.files.length;
        }
        scope.value.files = [];

        this.fileElement = element.find('file');

        scope.chooseFile = () => {
            this.fileElement.click();
        };

        this.fileElement.bind('change', (changeEvent) => {
            this.onFilesSelected(changeEvent);
        });

        scope.removeFile = (file: FileReadResponse) => {
            for (var i = 0; i < scope.value.files.length; i++) {
                if (scope.value.files[i].id == file.id) {
                    scope.value.files.splice(i, 1);
                    break;
                }
            }
        }
    }

    private onFilesSelected(changeEvent) {
        var sourceFiles = changeEvent.target.files;
        for (var i = 0; i < sourceFiles.length; i++) {
            var sourceFile = sourceFiles[i];
            this.readFile(sourceFile, changeEvent, sourceFiles);
        }
    }

    private onReadFinished(loadEvent, sourceFile, sourceFiles) {
        var value = loadEvent.target.result;
        var pos = value.indexOf("base64,");
        if (pos > 0) {
            value = value.substring(pos + 7);
        }
        var file: FileReadResponse = <FileReadResponse> {
            file: value,
            type: sourceFile.type,
            name: sourceFile.name,
            id: this.fileId++
        };

        this.scope.value.files.push(file);

        if (this.scope.value.files.length - this.fileReadBefore == sourceFiles.length) {
            this.fileElement.val('');
        }
    }

    private readFile(sourceFile, changeEvent, sourceFiles) {
        var fileName = sourceFile.name;
        var reader = new FileReader();
        reader.onload = function (loadEvent) {
            this.scope.$apply(function () {
                this.onReadFinished(changeEvent, sourceFile, sourceFiles);
            });
        };
        reader.readAsDataURL(sourceFile);
    }
}

import module = require('./module');

module.directive('multiFileRead', ['serverConfiguration', (serverConfiguration: ServerConfiguration) => {
    return <ng.IDirective>{
        templateUrl: serverConfiguration.rootPath + '/views/multiFileRead.html',
        restrict: 'E',
        replace: false,
        scope: {
            value: '='
        },
        link: MultiFileReadDirectiveLink
    };
}]);

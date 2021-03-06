/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import InputFormController = require('./InputFormController');
import CommandProcessorService = require('../services/CommandProcessorService');
import DialogControllerScope = require('./DialogControllerScope');

class DialogController extends InputFormController {

    private commandProcessor:CommandProcessorService;
    private qService:ng.IQService;
    private dialogScope:DialogControllerScope;

    constructor(scope:DialogControllerScope,
                dialogService:angular.material.MDDialogService,
                sceService:ng.ISCEService,
                serverConfiguration:ServerConfiguration,
                commandProcessor:CommandProcessorService,
                qService:ng.IQService) {

        super(scope, dialogService, sceService, serverConfiguration, scope.configuration.object, scope.configuration.definition, null,
            changedObject => this.successFunction(changedObject));

        this.commandProcessor = commandProcessor;
        this.qService = qService;
        this.dialogScope = scope;

        var configuration = scope.configuration;
        scope.configuration = null;
        scope.submitError = "";
        scope.submitSuccess = "";
        scope.hasChanges = false;
        scope.applyCaption = configuration.applyCaption;
        scope.changesApplied = false;

        scope.openForm = (form: ng.IFormController) => {
            form.$setPristine();
            scope.changesApplied = false;
            scope.submitError = "";
            scope.submitSuccess = "";
        };

        scope.$watch('object', (newValue, oldValue) => {
            if (newValue !== oldValue) {
                this.onDataChanged();
            }
        }, true);
    }

    protected okDisabled(form: ng.IFormController): boolean {
        return super.okDisabled(form) || !this.dialogScope.hasChanges;
    }

    private onDataChanged() {
        this.dialogScope.hasChanges = true;
        this.dialogScope.submitError = '';
        this.dialogScope.submitSuccess = '';
    }

    protected successFunction(changedObject):ng.IPromise<void> {
        var deferred:ng.IDeferred<void> = this.qService.defer<void>();

        this.commandProcessor.sendCommand("save", {changedObject: changedObject})
            .then((data:any) => {
                this.dialogScope.hasChanges = false;
                this.dialogScope.changesApplied = true;
                this.dialogScope.submitSuccess = data.message;
                deferred.resolve();
            }, (message:string) => deferred.reject(message));

        return deferred.promise;
    }
}

import module = require('./module');
module.controller("dialog", [
    '$scope',
    '$mdDialog',
    '$sce',
    'serverConfiguration',
    'commandProcessor',
    '$q',
    DialogController
]);

export = DialogController;

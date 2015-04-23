/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import app = require('app');

interface DialogScope extends angular.IScope {
    definition: string;
    object: any;
    submitError: string;
    submitSuccess: string;
    applyCaption: string;
    changesApplied: boolean;
    viewData: any;
    openForm();
}

export class DialogController {
    constructor($scope: DialogScope, $timeout, lazyLoad, $sce) {
        $scope.definition = $scope.viewData.definition;
        $scope.object = $scope.viewData.object;
        $scope.submitError = "";
        $scope.submitSuccess = "";
        $scope.applyCaption = $scope.viewData.applyCaption;
        $scope.changesApplied = false;
        $scope.openForm = () => {
            $scope.changesApplied = false;
            $scope.submitError = "";
            $scope.submitSuccess = "";
        }
    }

/*    successFunction(changedObject, success, fail) {
        $scope.sendCommand("save", {
            "changedObject": changedObject
        }, function(data) {
            $scope.hasChanges = false;
            $scope.changesApplied = true;
            $scope.submitSuccess = data.message;
            success();
        }, function(message) {
            fail(message);
        });
    }

    InputFormController($scope, null, $scope.definition, $scope.object, successFunction, $scope.getTypeahead, $sce);

    $scope.hasChanges = false;

    $scope.$watch("object", function() {
        $scope.hasChanges = true;
        $scope.resetAlerts();
        $scope.submitError = "";
        $scope.submitSuccess = "";
    }, true);

    $timeout(function() {
        $scope.hasChanges = false;
    });*/
}

//app.controller("dialog", DialogController);

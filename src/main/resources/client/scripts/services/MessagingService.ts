/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import $ = require('jquery');

interface MessageToastControllerScope extends ng.IScope {
    message: string;
    close: () => void;
}

class MessageToastController {
    constructor(scope: MessageToastControllerScope, toast: angular.material.MDToastService, message: string) {
        scope.message = message;
        scope.close = () => toast.hide();
    }
}

class MessagingService {

    private alerts: Alert[] = [];
    private alertId: number = 0;
    private version: number = 0;
    toastService: angular.material.MDToastService;
    serverConfiguration: ServerConfiguration;

    constructor(serverConfiguration: ServerConfiguration, toastService: angular.material.MDToastService) {
        this.toastService = toastService;
        this.serverConfiguration = serverConfiguration;
    }

    public getVersion(): number {
        return this.version;
    }

    public getAlerts(): Alert[] {
        var alerts = this.alerts;
        this.alerts = null;
        return alerts;
    }

    private showAlert(message:string, type: string): void {

        var options: any = {
            controller: ['$scope', '$mdToast', 'message', MessageToastController],
            templateUrl: this.serverConfiguration.rootPath + '/views/messageToast.html',
            locals: { message: message },
            position: 'top',
            parent: $('#alerts-anchor')
        };

        this.toastService.show(options);

        /*var alert: Alert = {
            message: message,
            type: type,
            id: this.alertId++
        };
        this.alerts.push(alert);
        this.version++;*/
    }

    public showError(message: string): void {
        this.showAlert(message, 'danger');
    }

    public showWarning(message: string): void {
        this.showAlert(message, 'warning');
    }

    public showMessage(message: string): void {
        this.showAlert(message, 'success');
    }

    resetAlerts() {
        this.alerts = [];
        this.version++;
    }
}

import servicesModule = require('./module');
servicesModule.service('messaging', ['serverConfiguration', '$mdToast', MessagingService]);

export = MessagingService;
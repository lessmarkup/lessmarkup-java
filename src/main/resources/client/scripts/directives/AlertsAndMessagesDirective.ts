/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import _ = require('lodash');

import MessagingService = require('../services/MessagingService');

interface AlertsAndMessagesDirectiveScope extends ng.IScope {
    alerts: Alert[];
    closeAlert(id: number): void;
}

class AlertsAndMessagesDirectiveController {

    private scope: AlertsAndMessagesDirectiveScope;
    private messagingService: MessagingService;

    constructor(scope: AlertsAndMessagesDirectiveScope, messagingService: MessagingService) {

        this.scope = scope;
        this.messagingService = messagingService;
        scope.alerts = [];
        scope.closeAlert = (id: number) => this.onCloseAlert(id);
        scope.$watch(() => messagingService.getVersion(), () => this.onAlertsChanged());
    }

    private onCloseAlert(id: number): void {
        for (var i = 0; i < this.scope.alerts.length; i++) {
            if (this.scope.alerts[i].id == id) {
                this.scope.alerts.splice(i, 1);
                break;
            }
        }
    }

    private onAlertsChanged() {
        var alerts: Alert[] = this.messagingService.getAlerts();
        if (alerts != null) {
            this.scope.alerts = _.union(this.scope.alerts, alerts);
        }
    }
}

import module = require('./module');

module.directive('alertsAndMessages', ['serverConfiguration', (serverConfiguration: ServerConfiguration) => {
    return <ng.IDirective>{
        templateUrl: serverConfiguration.rootPath + '/views/alertsAndMessages.html',
        restrict: 'E',
        replace: true,
        scope: true,
        controller: [
            '$scope',
            'messaging',
            AlertsAndMessagesDirectiveController
        ]
    };
}]);

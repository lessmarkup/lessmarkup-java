import _ = require('lodash');

import MessagingService = require('../services/MessagingService');

interface AlertsAndMessagesDirectiveScope extends ng.IScope {
    alerts: Alert[];
    closeAlert(id: number): void;
}

class AlertsAndMessagesDirectiveController {
    constructor(scope: AlertsAndMessagesDirectiveScope, messagingService: MessagingService) {
        scope.alerts = [];
        scope.closeAlert = (id: number):void => {
            for (var i = 0; i < scope.alerts.length; i++) {
                if (scope.alerts[i].id == id) {
                    scope.alerts.splice(i, 1);
                    break;
                }
            }
        };

        scope.$watch(() => {
            return messagingService.getVersion();
        }, () => {
            var alerts: Alert[] = messagingService.getAlerts();
            if (alerts != null) {
                scope.alerts = _.union(scope.alerts, alerts);
            }
        })
    }
}

import module = require('./module');

module.directive('alertsAndMessages', [() => {
    return <ng.IDirective>{
        templateUrl: '/views/alertsAndMessages.html',
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

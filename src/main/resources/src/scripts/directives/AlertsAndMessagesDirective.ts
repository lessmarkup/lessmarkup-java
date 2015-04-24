import ng = require('angular');
import _ = require('lodash');

interface AlertsAndMessagesDirectiveScope extends ng.IScope {
    alerts: Alert[];
    closeAlert(id: string): void;
}

class AlertsAndMessagesDirectiveController {
    constructor(scope: AlertsAndMessagesDirectiveScope, messagingService: MessagingService) {
        scope.alerts = [];
        scope.closeAlert = (id: string):void => {
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

import app = require('app');

app.directive('alertsAndMessages', ['serverConfiguration', (serverConfiguration: ServerConfiguration) => {
    return <ng.IDirective>{
        templateUrl: serverConfiguration.rootPath + '/views/alertsAndMessages.html',
        restrict: 'E',
        replace: true,
        scope: true,
        controller: ['$scope', AlertsAndMessagesDirectiveController]
    };
}]);
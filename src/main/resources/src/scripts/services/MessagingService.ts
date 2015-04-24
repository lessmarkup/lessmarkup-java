import ng = require('angular');

class MessagingService {

    private alerts: Alert[] = [];
    private alertId: number = 0;
    private version: number = 0;

    public getVersion(): number {
        return this.version;
    }

    public getAlerts(): Alert[] {
        var alerts = this.alerts;
        this.alerts = null;
        return alerts;
    }

    private showAlert(message:string, type: string): void {
        var alert: Alert = new {
            message: message,
            type: type,
            id: this.alertId++
        };
        this.alerts.push(alert);
        this.version++;
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
servicesModule.service('messaging', [MessagingService]);

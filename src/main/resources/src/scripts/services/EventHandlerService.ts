class EventHandlerService {
    private handlers: { [event: string]: IEventHandler[] } = {};

    public subscribeEvent(event: string, handler: IEventHandler) {
        var eventHandlers: IEventHandler[];
        if (!this.handlers.hasOwnProperty(event)) {
            eventHandlers = [];
            this.handlers[event] = eventHandlers;
        } else {
            eventHandlers = this.handlers[event];
        }
        eventHandlers.push(handler);
    }

    public unsubscribe(event: string, handler: IEventHandler) {
        if (!this.handlers.hasOwnProperty(event)) {
            return;
        }

        var eventHandlers: IEventHandler[] = this.handlers[event];
        var index:number = eventHandlers.indexOf(handler);
        if (index >= 0) {
            if (eventHandlers.length == 1) {
                delete this.handlers[event];
            } else {
                eventHandlers.slice(index, 1);
            }
        }
    }

    public invoke(event: string, sender: any) {
        if (!this.handlers.hasOwnProperty(event)) {
            return;
        }

        var eventHandlers: IEventHandler[] = this.handlers[event];

        for (var i:number = 0; i < eventHandlers.length; i++) {
            eventHandlers[i].onEvent(sender, true);
        }
    }

    public isSubscribed(event: string) : boolean {
        return this.handlers.hasOwnProperty(event);
    }

    public isEnabled(event: string, sender: any) : boolean {
        if (!this.handlers.hasOwnProperty(event)) {
            return false;
        }

        var eventHandlers: IEventHandler[] = this.handlers[event];

        for (var i:number = 0; i < eventHandlers.length; i++) {
            if (eventHandlers[i].onEvent(sender, false)) {
                return true;
            }
        }

        return false;
    }

    reset() {
        this.handlers = {};
    }
}

import servicesModule = require('./module');
servicesModule.service('eventHandler', [
    EventHandlerService]);

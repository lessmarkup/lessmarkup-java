interface ICommandHandler {
    onCommand(sender: any, invoke: boolean);
}

class CommandHandler {
    handlers: { [command: string]: ICommandHandler[] } = {};

    subscribe(command: string, handler: ICommandHandler) {
        var commandHandlers: ICommandHandler[];
        if (!this.handlers.hasOwnProperty(command)) {
            commandHandlers = [];
            this.handlers[command] = commandHandlers;
        } else {
            commandHandlers = this.handlers[command];
        }
        commandHandlers.push(handler);
    }

    unsubscribe(command: string, handler: ICommandHandler) {
        if (!this.handlers.hasOwnProperty(command)) {
            return;
        }

        var commandHandlers: ICommandHandler[] = this.handlers[command];
        var index:number = commandHandlers.indexOf(handler);
        if (index >= 0) {
            if (commandHandlers.length == 1) {
                delete this.handlers[command];
            } else {
                commandHandlers.slice(index, 1);
            }
        }
    }

    invoke(command: string, sender: any) {
        if (!this.handlers.hasOwnProperty(command)) {
            return;
        }

        var commandHandlers: ICommandHandler[] = this.handlers[command];

        for (var i:number = 0; i < commandHandlers.length; i++) {
            commandHandlers[i].onCommand(sender, true);
        }
    }

    isSubscribed(command: string) : boolean {
        return this.handlers.hasOwnProperty(command);
    }

    isEnabled(command: string, sender: any) : boolean {
        if (!this.handlers.hasOwnProperty(command)) {
            return false;
        }

        var commandHandlers: ICommandHandler[] = this.handlers[command];

        for (var i:number = 0; i < commandHandlers.length; i++) {
            if (commandHandlers[i].onCommand(sender, false)) {
                return true;
            }
        }

        return false;
    }

    reset() {
        this.handlers = {};
    }
}

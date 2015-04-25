import _ = require('lodash');

export class WebSocketConnection {

    private webSocket: WebSocket;

    onOpen(event) {
    }

    onClose() {
    }

    onError() {
    }

    private uintToString(uintArray) {
        return decodeURIComponent(encodeURI(String.fromCharCode.apply(null, uintArray)));
    }

    private onMessage(event) {
        if (typeof (event.data) === "string") {
            var pos = event.data.indexOf(';');
            if (pos <= 0) {
                return;
            }

            var methodName = event.data.substr(0, pos).toLowerCase();
            pos++;
            var parameters = pos < event.data.length ? JSON.parse(event.data.substr(pos)) : null;

            _.forOwn(this, (value, key) => {
                if (key.toLowerCase() === methodName) {
                    methodName = key;
                }
            });

            var func = this[methodName];

            if (typeof (func) === "function") {
                func(parameters);
            }
        } else {
            // here we work with binary data
            var byteArray = new Uint8Array(event.data);
            var byteArrayBuffer: any[] = <any> byteArray.buffer;
            var methodNameLength = new Uint32Array(byteArrayBuffer.slice(0, 4))[0];
            var valuesLength = new Uint32Array(byteArrayBuffer.slice(4, 8))[0];
            var methodName:any = this.uintToString(new Uint8Array(byteArrayBuffer.slice(8, methodNameLength + 8))).toLowerCase();
            var parametersText = "";
            var parameters = null;
            if (valuesLength > 0) {
                parametersText = this.uintToString(new Uint8Array(byteArrayBuffer.slice(8 + methodNameLength, 8 + methodNameLength + valuesLength)));
                parameters = JSON.parse(parametersText);
            }

            _.forOwn(this, (value, key) => {
                if (key.toLowerCase() == methodName) {
                    methodName = key;
                }
            });

            var func = this[methodName];
            if (typeof (func) == "function") {
                func(byteArray, 8 + methodNameLength + valuesLength, byteArray.length - 8 - methodNameLength - valuesLength, parameters);
            }
        }
    }

    public open(url: string) {
        try {
            this.webSocket = new WebSocket(url);
            this.webSocket.binaryType = "arraybuffer";
            this.webSocket.onmessage = this.onMessage;

            this.webSocket.onopen = (event) => {
                this.onOpen(event);
            };

            this.webSocket.onerror = (event) => {
                this.onError();
            };

            this.webSocket.onclose = () => {
                this.onClose();
            }

        } catch (exception) {
            this.onError();
        }
    }

    public sendRequest(methodName, parameters) {
        var parametersText = "";
        if (parameters != null) {
            parametersText = JSON.stringify(parameters);
        }
        var fullText = methodName + ";" + parametersText;
        this.webSocket.send(fullText);
    }

    public close() {
        this.webSocket.close();
    }
}

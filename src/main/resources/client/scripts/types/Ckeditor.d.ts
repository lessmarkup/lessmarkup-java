// This definition is a short version of https://github.com/borisyankov/DefinitelyTyped/blob/master/ckeditor/ckeditor.d.ts
// to work with AMD as original version works only with globally defined CKEDITOR variable

declare module "ckeditor" {

    interface EventInfo {
        data: any;
        editor: Editor;
        listenerData: any;
        name: string;
        sender: any;
        cancel(): void;
        removeListener(): void;
        stop(): void;
    }

    class Editor {
        getData(noEvents?: Object): string;
        setData(data: string, options?: { internal?: boolean; callback?: Function; noSnapshot?: boolean; }): void;
        on(eventName: string, listenerFunction: (eventInfo: EventInfo) => void, scopeObj?: Object, listenerData?: Object, priority?: number): Object;
    }

    function replace(element: string, config?: any): Editor;
    function replace(element: HTMLTextAreaElement, config?: any): Editor;
}

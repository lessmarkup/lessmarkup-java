class RecordListAction {
    name: string;
    type: string;
    visible: (record: RecordListRecord) => boolean;
    parameter: string;
}
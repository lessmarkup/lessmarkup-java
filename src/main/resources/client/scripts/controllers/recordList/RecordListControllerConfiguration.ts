interface RecordListControllerConfiguration {
    columns: RecordListColumn[];
    links: RecordListLink[];
    optionsTemplate: string;
    manualRefresh: boolean;
    hasSearch: boolean;
    actions: RecordListActionDefinition[];
    type: string;
    recordIds: number[];
    records: RecordListRecord[];
    extensionScript: string;
}

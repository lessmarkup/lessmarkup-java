class ServerResponse {
    success: boolean;
    message: string;
    data: any;
    user: UserStateUpdate;
    versionId: number;
    collectionChanges: CollectionStateChange[];
    updates: ServerResponseUpdates;
    collections: CollectionDefinition[];
    topMenu: TopMenuItem[];
    navigationTree: MenuItem[];
    require: string[];
}

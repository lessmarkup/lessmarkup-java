class ServerResponse<T> {
    success: boolean;
    message: string;
    data: T;
    user: UserStateUpdate;
    versionId: number;
    collectionChanges: CollectionStateChange[];
    updates: ServerResponseUpdates;
    collections: CollectionDefinition[];
    topMenu: TopMenuItem[];
    navigationTree: MenuItem[];
    require: string[];
}

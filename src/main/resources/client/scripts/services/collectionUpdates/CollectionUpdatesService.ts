class CollectionUpdatesService {
    private collections: CollectionDefinition[];
    private version: number = 0;

    constructor(serverConfiguration: ServerConfiguration) {
        this.collections = serverConfiguration.collections || [];
    }

    public getCollections(): CollectionDefinition[] {
        return this.collections;
    }

    public onCollections(collections: CollectionDefinition[]) : void {
        this.collections = collections;
        this.version++;
    }

    public onCollectionStateChanged(changes: CollectionStateChange[]): void {
        if (changes.length == 0) {
            return;
        }

        var hasChanges = false;

        for (var i = 0; i < changes.length; i++) {
            var change: CollectionStateChange = changes[i];
            for (var j = 0; j < this.collections.length; j++) {
                var collection = this.collections[j];
                if (collection.id == change.id) {
                    if (change.change > 0) {
                        collection.count += change.change;
                        hasChanges = true;
                    } else {
                        collection.count = change.newValue;
                        hasChanges = true;
                    }
                    break;
                }
            }
        }

        if (hasChanges) {
            this.version++;
        }
    }

    public getVersion():number {
        return this.version;
    }
}

import module = require('../module');
module.service('collectionUpdates', ['serverConfiguration', CollectionUpdatesService]);

export = CollectionUpdatesService;
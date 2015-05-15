/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import RecordListRecord = require('../../datatypes/RecordListRecord')
import RecordListColumn = require('../../datatypes/RecordListColumn');

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

export = RecordListControllerConfiguration;
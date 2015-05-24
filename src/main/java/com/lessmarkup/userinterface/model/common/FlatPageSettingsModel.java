/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.userinterface.model.common;

import com.lessmarkup.TextIds;
import com.lessmarkup.interfaces.annotations.InputField;
import com.lessmarkup.interfaces.recordmodel.InputFieldType;
import com.lessmarkup.interfaces.recordmodel.RecordModel;

public class FlatPageSettingsModel extends RecordModel<FlatPageSettingsModel> {
    private boolean loadOnShow;
    private FlatPagePosition position;
    private int levelToLoad;
    
    public FlatPageSettingsModel() {
        super(TextIds.FLAT_PAGE_SETTINGS);
    }

    @InputField(type = InputFieldType.CHECK_BOX, textId = TextIds.LOAD_ON_SHOW, defaultValue = "false")
    public void setLoadOnShow(boolean loadOnShow) { this.loadOnShow = loadOnShow; }
    public boolean isLoadOnShow() { return loadOnShow; }

    @InputField(type = InputFieldType.SELECT, textId = TextIds.POSITION, defaultValue = "RIGHT")
    public void setPosition(FlatPagePosition position) { this.position = position; }
    public FlatPagePosition getPosition() { return position; }

    @InputField(type = InputFieldType.NUMBER, textId = TextIds.LEVEL_TO_LOAD, defaultValue = "2")
    public void setLevelToLoad(int levelToLoad) { this.levelToLoad = levelToLoad; }
    public int getLevelToLoad() { return levelToLoad; }
}

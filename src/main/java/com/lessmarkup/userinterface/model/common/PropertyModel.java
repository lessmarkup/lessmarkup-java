/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.userinterface.model.common;

import com.lessmarkup.interfaces.recordmodel.InputFieldType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class PropertyModel {
    private String name;
    private Object value;
    private InputFieldType type;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }

    public InputFieldType getType() {
        return type;
    }
    public void setType(InputFieldType type) {
        this.type = type;
    }
}

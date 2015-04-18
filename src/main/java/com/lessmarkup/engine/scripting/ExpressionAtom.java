/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.scripting;

import com.google.gson.JsonElement;

class ExpressionAtom {
    private AtomType type;
    private JsonElement value;

    public ExpressionAtom(AtomType type) {
        this.type = type;
    }
    
    public ExpressionAtom(AtomType type, JsonElement value) {
        this.type = type;
        this.value = value;
    }
    
    /**
     * @return the type
     */
    public AtomType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(AtomType type) {
        this.type = type;
    }

    /**
     * @return the value
     */
    public JsonElement getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(JsonElement value) {
        this.value = value;
    }
}

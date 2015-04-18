/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.filesystem;

public class XmlMinifyResource {
    private String minified;
    private String plain;

    public String getMinified() {
        return minified;
    }
    public void setMinified(String minified) {
        this.minified = minified;
    }

    public String getPlain() {
        return plain;
    }
    public void setPlain(String plain) {
        this.plain = plain;
    }
}

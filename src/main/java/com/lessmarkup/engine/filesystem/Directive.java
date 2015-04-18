/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.filesystem;

class Directive {
    private DirectiveType directiveType;
    private String body;

    DirectiveType getDirectiveType() { return directiveType; }
    void setDirectiveType(DirectiveType directiveType) { this.directiveType = directiveType; }

    String getBody() { return body; }
    void setBody(String body) { this.body = body; }
}

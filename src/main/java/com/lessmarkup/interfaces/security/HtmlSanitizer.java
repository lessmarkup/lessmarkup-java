/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.interfaces.security;

public interface HtmlSanitizer {
    String sanitize(String html, String[] tagsToRemove);
    default String sanitize(String html) {
        return sanitize(html, null);
    }
}

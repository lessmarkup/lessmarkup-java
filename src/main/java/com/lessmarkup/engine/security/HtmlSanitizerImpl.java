/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.security;

import com.lessmarkup.interfaces.security.HtmlSanitizer;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

public class HtmlSanitizerImpl implements HtmlSanitizer {

    @Override
    public String sanitize(String html, String[] tagsToRemove) {
        PolicyFactory sanitizer = Sanitizers.FORMATTING;

        if (tagsToRemove != null) {
            sanitizer = sanitizer.and(new HtmlPolicyBuilder().disallowElements(tagsToRemove).toFactory());
        }
        
        return sanitizer.sanitize(html);
    }
}

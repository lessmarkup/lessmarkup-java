/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.security;

public final class EmailCheck {
    
    private static final String PATTERN = "^[a-z][a-z|0-9|]*([_][a-z|0-9]+)*([.][a-z|0-9]+([_][a-z|0-9]+)*)?@[a-z][a-z|0-9|]*\\.([a-z][a-z|0-9]*(\\.[a-z][a-z|0-9]*)?)$";
    private static final int MAXIMUM_EMAIL_LENGTH = 100;
    
    public static boolean isValidEmail(String email) {
        
        if (email == null) {
            return false;
        }
        
        email = email.trim();
        
        if (email.length() == 0) {
            return false;
        }
        
        if (email.length() > MAXIMUM_EMAIL_LENGTH) {
            return false;
        }
        
        return email.matches(PATTERN);
    }
}

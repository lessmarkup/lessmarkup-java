/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.security;

public class TextValidator {
    private static final int MaximumUsernameLength = 30;
    private static final int MaximumPasswordLength = 100;
    private static final int MaximumTextLength = 128;
    private static final int MinimumPasswordLetterOrDigit = 4;
    private static final int MinimumUsernameLength = 6;
    private static final int MinimumPasswordLength = 7;

    public static boolean checkPassword(String text) {
        return checkString(text, MinimumPasswordLength, MaximumPasswordLength);
    }

    public static boolean checkNewPassword(String text)
    {
        if (!checkPassword(text))
        {
            return false;
        }
        
        boolean hasDigit = false;
        boolean hasUpper = false;
        boolean hasLower = false;
        int lettersOrDigits = 0;
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            
            if (Character.isLetterOrDigit(c)) {
                lettersOrDigits++;
            }
            
            if (Character.isDigit(c)) {
                hasDigit = true;
                continue;
            }
            
            if (Character.isUpperCase(c)) {
                hasUpper = true;
                continue;
            }
            
            if (Character.isLowerCase(c)) {
                hasLower = true;
            }
        }
        
        return !(!hasDigit || !hasUpper || hasLower || lettersOrDigits < MinimumPasswordLetterOrDigit);
    }

    public static boolean checkUsername(String text)
    {
        return checkString(text, MinimumUsernameLength, MaximumUsernameLength);
    }

    public static boolean checkTextField(String text)
    {
        return checkString(text, 0, MaximumTextLength);
    }

    private static boolean checkString(String text, int minLength, int maxLength)
    {
        if (text == null || text.length() == 0) {
            return minLength == 0;
        }
        
        if (text.length() > maxLength) {
            return false;
        }
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            
            if (Character.isISOControl(c) || Character.isSpaceChar(c) || Character.isWhitespace(c)) {
                return false;
            }
        }
        
        return true;
    }
}

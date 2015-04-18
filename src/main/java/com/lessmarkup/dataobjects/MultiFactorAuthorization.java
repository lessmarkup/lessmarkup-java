/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.dataobjects;

/**
 *
 * @author User
 */
public enum MultiFactorAuthorization {
    NONE(0),
    ALWAYS(1),
    CHANGED_IP(2);
    
    private final int value;
    
    MultiFactorAuthorization(int value) {
        this.value = value;
    }
    
    public static MultiFactorAuthorization of(int value) {
        switch (value) {
            case 0:
                return NONE;
            case 1:
                return ALWAYS;
            case 2:
                return CHANGED_IP;
            default:
                return null;
        }
    }
    
    public int getValue() {
        return this.value;
    }
}

package com.lessmarkup.framework.helpers;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public final class StringHelper {
    
    public static String join(String delimiter, Iterable<String> items) {
        return join(delimiter, items.iterator());
    }
    
    public static String getMessage(Throwable e) {
        String message = e.getMessage();
        if (message != null) {
            return message;
        }
        return e.toString();
    }
    
    public static String binaryToString(byte[] binary) {
        if (binary.length >= 3 && binary[0] == -17 && binary[1] == -69 && binary[2] == -65) { // Unicode marker
            return new String(binary, 3, binary.length-3, StandardCharsets.UTF_8);
        }
        return new String(binary, StandardCharsets.UTF_8);
    }
    
    public static String join(String delimiter, Iterator<String> items) {
        StringBuilder ret = new StringBuilder();
        while (items.hasNext()) {
            String item = items.next();
            if (ret.length() > 0) {
                ret.append(delimiter);
            }
            ret.append(item);
        }
        return ret.toString();
    }
    
    public static String toJsonCase(String source) {
        if (source == null || source.length() == 0) {
            return source;
        }
        return source.substring(0, 1).toLowerCase() + source.substring(1);
    }
    
    public static String fromJsonCase(String source) {
        if (source == null || source.length() == 0) {
            return source;
        }
        return source.substring(0, 1).toUpperCase() + source.substring(1);
    }

    public static boolean isNullOrEmpty(String text) {
        return text == null || text.isEmpty();
    }

    public static boolean isNullOrWhitespace(String text) {
        if (text == null || text.isEmpty()) {
            return true;
        }
        return text.trim().isEmpty();
    }
}

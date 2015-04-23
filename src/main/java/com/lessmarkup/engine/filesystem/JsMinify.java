/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.filesystem;

public class JsMinify implements Minify {

    private static boolean isControlChar(char c)
    {
        return c == ',' || c == ':' || c == ')' || c == '(' || c == ';' || c == '=' || c == '+' || c == '-' || c == '!' || c == '&' || c == '|' || c == '?' || c == '}' || c == '{' || c == '<' || c == '>';
    }
    
    @Override
    public String process(String source) {
        StringBuilder result = new StringBuilder();
        boolean lastSpace = false;
        boolean anySymbol = false;
        boolean addSpace = true;
        char lastChar = '\0';
        boolean addReturn = false;

        for (int pos = 0; pos < source.length(); )
        {
            char c = source.charAt(pos);
            
            /*if (pos == 0 && c == 65279) { // Unicode marker
                pos++;
                continue;
            }*/

            if (c == '{' || c == '}') {
                anySymbol = false;
                addSpace = false;
                lastSpace = false;
                addReturn = false;
                result.append(c);
                pos++;
                continue;
            }

            if (c == ' ' || c == '\r' || c == '\n' || c == '\t' || c == '\b') {
                if (c == '\r' || c == '\n') {
                    addReturn = true;
                }

                if (anySymbol && !lastSpace) {
                    addSpace = true;
                    lastSpace = true;
                }

                pos++;
                continue;
            }

            if (c == '/' && pos + 1 < source.length()) {
                char c1 = source.charAt(pos + 1);
                if (c1 == '*') {
                    for (pos += 2; pos < source.length(); pos++) {
                        c = source.charAt(pos);
                        if (c != '*') {
                            continue;
                        }
                        if (pos + 1 < source.length() && source.charAt(pos + 1) == '/') {
                            pos += 2;
                            break;
                        }
                    }
                    continue;
                }

                if (c1 == '/') {
                    for (pos += 2; pos < source.length(); pos++) {
                        c = source.charAt(pos);
                        if (c == '\r' || c == '\n') {
                            break;
                        }
                    }
                    addSpace = true;
                    anySymbol = true;
                    lastSpace = false;
                    continue;
                }
            }

            if (addReturn) {
                addSpace = false;
                //anySymbol = false;
                result.append('\n');
                addReturn = false;
            }

            if (addSpace) {
                if (!isControlChar(c)) {
                    result.append(' ');
                }
                addSpace = false;
            }

            if (c == '/' && isControlChar(lastChar)) {
                int start = pos;
                boolean insidePattern = false;
                for (pos += 1; pos < source.length(); pos++) {
                    c = source.charAt(pos);
                    if (c == '\\') {
                        pos++;
                        continue;
                    }
                    if (c == '[') {
                        insidePattern = true;
                        continue;
                    }
                    if (c == ']') {
                        insidePattern = false;
                        continue;
                    }
                    if (c == '/' && !insidePattern) {
                        pos++;
                        for (; pos < source.length(); pos++) {
                            c = source.charAt(pos);
                            if (c != 'g' && c != 'i' && c != 'm' && c != 'y') {
                                break;
                            }
                        }
                        result.append(source.substring(start, pos));
                        break;
                    }
                    if (c == '\r' || c == '\n') {
                        result.append(source.substring(start, pos));
                        pos++;
                        break;
                    }
                }
                anySymbol = true;
                //addSpace = false;
                lastChar = '/';
                continue;
            }

            anySymbol = true;
            lastSpace = isControlChar(c);

            if (c == '\'' || c == '\"') {
                char delimiter = c;
                int s;
                boolean found = false;
                for (s = pos + 1; s < source.length(); s++) {
                    c = source.charAt(s);
                    if (c == '\\') {
                        s++;
                        continue;
                    }
                    if (c == delimiter) {
                        result.append(source.substring(pos, s + 1));
                        pos = s + 1;
                        found = true;
                        break;
                    }

                    if (c == '\r' || c == '\n') {
                        result.append(source.substring(pos, s));
                        pos = s + 1;
                        found = true;
                        break;
                    }

                    if (c == '\\') {
                        s++;
                    }
                }
                if (!found) {
                    result.append(delimiter);
                    pos++;
                }
                lastChar = delimiter;
                continue;
            }

            result.append(c);
            pos++;
            lastChar = c;
        }

        return result.toString();
    }
}
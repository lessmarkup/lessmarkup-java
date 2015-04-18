/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.filesystem;

class CssMinify implements Minify {

    @Override
    public String process(String source) {
        StringBuilder result = new StringBuilder();
        
        boolean lastSpace = false;
        boolean anySymbol = false;
        boolean addSpace = true;
        
        for (int pos = 0; pos < source.length(); ) {
            
            char c = source.charAt(pos);
            
            /*if (pos == 0 && c == 65279) { // Unicode marker
                pos++;
                continue;
            }*/
            
            if (c == '{' || c == '}') {
                anySymbol = false;
                addSpace = false;
                lastSpace = false;
                result.append(c);
                pos++;
                continue;
            }
            
            if (c == ' ' || c == '\r' || c == '\n' || c == '\t' || c == '\b')
            {
                if (anySymbol && !lastSpace)
                {
                    addSpace = true;
                    lastSpace = true;
                }

                pos++;
                continue;
            }

            if (c == '/' && pos + 1 < source.length() && source.charAt(pos + 1) == '*')
            {
                for (pos += 2; pos < source.length(); pos++)
                {
                    c = source.charAt(pos);
                    if (c != '*')
                    {
                        continue;
                    }
                    if (pos + 1 < source.length() && source.charAt(pos + 1) == '/')
                    {
                        pos += 2;
                        break;
                    }
                }
                continue;
            }

            if (addSpace)
            {
                if (c != '(' && c != ')' && c != ',' && c != ':' && c != ';')
                {
                    result.append(' ');
                }
                addSpace = false;
            }

            anySymbol = true;
            lastSpace = c == ',' || c == ':' || c == ')' || c == '(' || c == ';';

            if (c == '\'' || c == '\"')
            {
                char delimiter = c;
                int s;
                boolean found = false;
                for (s = pos + 1; s < source.length(); s++)
                {
                    c = source.charAt(s);
                    if (c == '\\')
                    {
                        s++;
                        continue;
                    }
                    if (c == delimiter)
                    {
                        result.append(source.substring(pos, s + 1));
                        pos = s + 1;
                        found = true;
                        break;
                    }
                    if (c == '\\')
                    {
                        s++;
                    }
                }
                if (!found)
                {
                    result.append(delimiter);
                    pos++;
                }
                continue;
            }

            result.append(c);
            pos++;
        }
        
        return result.toString();
    }
}

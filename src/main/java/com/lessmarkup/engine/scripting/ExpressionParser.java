/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.scripting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.List;

class ExpressionParser {
    
    private final List<ExpressionAtom> atoms = new ArrayList<>();
    
    public void parse(String expression) {
        for (int i = 0; i < expression.length();) {
            char c = expression.charAt(i);
            if (c == ' ' || c == '\r' || c == '\n' || c == '\t') {
                i++;
                continue;
            }
            if (c == '=' && i + 1 < expression.length() && expression.charAt(i+1) == '=') {
                i += 2;
                atoms.add(new ExpressionAtom(AtomType.EQUAL));
                continue;
            }
            if (c == '!' && i + 1 < expression.length() && expression.charAt(i + 1) == '=') {
                i += 2;
                atoms.add(new ExpressionAtom(AtomType.NOT_EQUAL));
                continue;
            }
            if (c == '!') {
                i += 1;
                atoms.add(new ExpressionAtom(AtomType.INVERSE));
                continue;
            }
            if (c == '\'' || c == '"') {
                int next = expression.indexOf(c, i + 1);
                if (next > i) {
                    atoms.add(new ExpressionAtom(AtomType.OBJECT, new JsonPrimitive(expression.substring(i+1, next))));
                    i = next + 1;
                    continue;
                }
            }
            if (c == '(') {
                atoms.add(new ExpressionAtom(AtomType.OPEN));
                i += 1;
                continue;
            }
            if (c == ')') {
                atoms.add(new ExpressionAtom(AtomType.CLOSE));
                i += 1;
                continue;
            }
            if (c == '&' && i + 1 < expression.length() && expression.charAt(i + 1) == '&') {
                atoms.add(new ExpressionAtom(AtomType.AND));
                i += 2;
                continue;
            }
            if (c == '|' && i + 1 < expression.length() && expression.charAt(i + 1) == '|') {
                atoms.add(new ExpressionAtom(AtomType.OR));
                i += 2;
                continue;
            }
            if (c == '+') {
                atoms.add(new ExpressionAtom(AtomType.PLUS));
                i += 1;
                continue;
            }
            if (c == '-') {
                atoms.add(new ExpressionAtom(AtomType.MINUS));
                i += 1;
                continue;
            }

            if (!Character.isLetterOrDigit(c)) {
                throw new IllegalArgumentException("Encountered unknown symbol '" + c + "' at position " + i);
            }

            int start = i;
            for (i++; i < expression.length(); i++) {
                c = expression.charAt(i);
                if (!Character.isLetterOrDigit(c) && c != '.') {
                    break;
                }
            }

            String parameterName = expression.substring(start, i);

            if (Character.isDigit(c)) {
                Integer intValue = Integer.parseInt(parameterName);
                atoms.add(new ExpressionAtom(AtomType.OBJECT, new JsonPrimitive(intValue)));
                continue;
            }

            if ("true".equals(parameterName))
            {
                atoms.add(new ExpressionAtom(AtomType.OBJECT, new JsonPrimitive(true)));
                continue;
            }

            if ("false".equals(parameterName)) {
                atoms.add(new ExpressionAtom(AtomType.OBJECT, new JsonPrimitive(false)));
                continue;
            }

            if ("null".equals(parameterName)) {
                atoms.add(new ExpressionAtom(AtomType.NULL));
                continue;
            }

            atoms.add(new ExpressionAtom(AtomType.PARAMETER, new JsonPrimitive(parameterName)));
        }
    }
    
    public boolean evaluate(JsonElement objectToEvaluate) {
        ScriptEvaluator evaluator = new ScriptEvaluator(atoms, objectToEvaluate);
        return ScriptEvaluator.toBoolean(evaluator.evaluate());
    }
}

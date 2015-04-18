/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.scripting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.lessmarkup.interfaces.structure.Tuple;
import java.util.ArrayList;
import java.util.List;

class ScriptEvaluator {
    
    private final List<ExpressionAtom> atoms;
    private final JsonElement root;
    //private final Map<String, JsonElement> properties = new HashMap<>();
    //private final List<JsonElement> elements = new LinkedList<>();
    
    public ScriptEvaluator(List<ExpressionAtom> atoms, JsonElement objectToEvaluate) {
        this.atoms = atoms;
        this.root = objectToEvaluate;
        
       
        /*Arrays.stream(objectToEvaluate.getClass().getMethods())
            .filter(m -> (m.getModifiers() & Modifier.STATIC) == 0 && m.getName().startsWith("get"))
            .forEach(method -> {
                String methodName = method.getName().substring(3);
                if (methodName.length() < 2 || !Character.isUpperCase(methodName.charAt(0))) {
                    return;
                }
                addProperty("", method, objectToEvaluate);
            });*/
    }
    
    /*private void addProperty(String prefix, Method method, Object instance) {
        Object childInstance;
        try {
            childInstance = method.invoke(instance);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(ScriptEvaluator.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        
        String methodName = method.getName().substring(3);
        
        if (childInstance == null || method.getReturnType().isPrimitive() || method.getReturnType().isEnum() || method.getReturnType() == String.class) {
            properties.put(StringHelper.toJsonCase(prefix + methodName), childInstance);
            return;
        }
        
        if (!method.getReturnType().isSynthetic()) {
            return;
        }
        
        Arrays.stream(method.getReturnType().getMethods())
            .filter(m -> (m.getModifiers() & Modifier.STATIC) == 0 && m.getName().startsWith("get"))
            .forEach(m -> {
                String name = m.getName().substring(3);
                if (name.length() < 2 || !Character.isUpperCase(name.charAt(0))) {
                    return;
                }
                addProperty(prefix + name + ".", m, childInstance);
            });
    }*/
    
    public JsonElement evaluate() {
        return evaluateGroup(0, atoms.size());
    }
    
    public static boolean toBoolean(JsonElement result) {
        if (result == null) {
            return false;
        }
        
        if (result.isJsonArray()) {
            return result.getAsJsonArray().size() > 0;
        }
        
        if (!result.isJsonPrimitive()) {
            return false;
        }
        
        return result.getAsBoolean();
        
        /*if (result.getClass() == Boolean.class) {
            return (Boolean) result;
        }

        String stringOperand = (String) result;
        boolean boolRet = "true".equals(stringOperand);
        if (!boolRet && !"false".equals(stringOperand)) {
            throw new IllegalArgumentException("Cannot convert '" + result + "' to boolean value");
        }
        return boolRet;*/
    }
    
    public static int toInt(JsonElement value) {
        if (value == null || !value.isJsonPrimitive()) {
            return 0;
        }
        
        return value.getAsInt();
        
        /*if (value.getClass() == Integer.class) {
            return (Integer) value;
        }

        int intValue = new Integer(value.toString());

        return intValue;*/
    }

    private static int getOperationPriority(AtomType atomType) {
        switch (atomType)
        {
            case AND:
            case OR:
                return 10;
            case MINUS:
            case PLUS:
                return 9;
            case EQUAL:
            case NOT_EQUAL:
                return 8;
        }

        return -1;
    }
    
    private JsonElement evaluateFlatOperations(List<ExpressionAtom> atoms, int from, int count)
    {
        if (count >= 3) {
            int to = from + count;
            int topPriority = -1;
            int index = -1;
            for (int i = from; i < to; i++) {
                int priority = getOperationPriority(atoms.get(i).getType());
                if (priority > topPriority) {
                    topPriority = priority;
                    index = i;
                }
            }
            int leftCount = index-from;
            int rightCount = from + count - index - 1;

            JsonElement right = evaluateFlatOperations(atoms, index + 1, rightCount);
            JsonElement left = evaluateFlatOperations(atoms, 0, leftCount);

            return executeBinaryOperator(atoms.get(index).getType(), left, right);
        }

        if (count == 1) {
            return atoms.get(from).getValue();
        }

        throw new IllegalArgumentException("Unexpected atoms count");
    }
    
    private JsonElement evaluateGroup(int start, int count) {
        int end = start + count;
        int pos = start;
        Tuple<JsonElement, Integer> result = evaluate(start, count);
        JsonElement left = result.getValue1();
        count = result.getValue2();
        pos += count;
        if (pos == end) {
            return left;
        }

        List<ExpressionAtom> childAtoms = new ArrayList<>();
        
        childAtoms.add(new ExpressionAtom(AtomType.OBJECT, left));

        while (pos < end) {
            ExpressionAtom op = childAtoms.get(pos);
            pos++;
            int rest = end - pos;
            if (!isBinaryOperator(op.getType())) {
                throw new IllegalArgumentException("Expected binary operator instead of '" + op.getType() + "'");
            }
            if (rest == 0) {
                throw new IllegalArgumentException("Expected binary operator right operand for '" + op.getType() + "'");
            }
            childAtoms.add(op);
            result = evaluate(pos, rest);
            rest = result.getValue2();
            childAtoms.add(new ExpressionAtom(AtomType.OBJECT, result.getValue1()));
            pos += rest;
        }

        return evaluateFlatOperations(childAtoms, 0, childAtoms.size());
    }
    
    private static boolean isBinaryOperator(AtomType atomType) {
        switch (atomType)
        {
            case AND:
            case OR:
            case EQUAL:
            case MINUS:
            case PLUS:
            case NOT_EQUAL:
                return true;
        }
        return false;
    }
    
    private static boolean executeEqual(JsonElement left, JsonElement right) {
        return left.equals(right);
    }

    private static JsonElement executeBinaryOperator(AtomType atomType, JsonElement left, JsonElement right) {
        switch (atomType) {
            case AND: {
                Boolean ret = toBoolean(left) && toBoolean(right);
                return new JsonPrimitive(ret);
            }
            case OR: {
                Boolean ret = toBoolean(left) || toBoolean(right);
                return new JsonPrimitive(ret);
            }
            case EQUAL:
                return new JsonPrimitive(executeEqual(left, right));
            case MINUS: {
                Integer ret = toInt(left) - toInt(right);
                return new JsonPrimitive(ret);
            }
            case PLUS: {
                Integer ret = toInt(left) + toInt(right);
                return new JsonPrimitive(ret);
            }
            case NOT_EQUAL: {
                Boolean ret = !executeEqual(left, right);
                return new JsonPrimitive(ret);
            }
        }

        throw new IllegalArgumentException("atomType");
    }

    private static boolean isUnaryOperator(AtomType atomType) {
        switch (atomType)
        {
            case INVERSE:
                return true;
        }
        return false;
    }

    private static JsonElement executeUnaryOperator(AtomType atomType, JsonElement operand)
    {
        switch (atomType)
        {
            case INVERSE:
                Boolean ret = !toBoolean(operand);
                return new JsonPrimitive(ret);
        }

        throw new IllegalArgumentException("atomType");
    }

    private Tuple<JsonElement, Integer> evaluate(int start, int count) {
        if (count == 0) {
            throw new IllegalArgumentException("count");
        }

        ExpressionAtom atom = atoms.get(start);

        switch (atom.getType()) {
            case PARAMETER:
                String parameterName = atom.getValue().toString();
                if (!root.isJsonObject()) {
                    throw new IllegalArgumentException("Unknown parameter '" + parameterName + "'");
                }
                JsonElement parameterValue = root.getAsJsonObject().get(parameterName);
                if (parameterValue == null)
                {
                    throw new IllegalArgumentException("Unknown parameter '" + parameterName + "'");
                }
                return new Tuple<>(parameterValue, 1);
            case NULL:
                return new Tuple<>(null, 1);
            case OBJECT:
                return new Tuple<>(atom.getValue(), 1);
        }

        if (isUnaryOperator(atom.getType())) {
            if (count == 1) {
                throw new IllegalArgumentException("Expected unary operator operand for '" + atom.getType() + "'");
            }

            int nextCount = count - 1;
            int nextStart = start + 1;

            Tuple<JsonElement, Integer> result = evaluate(nextStart, nextCount);

            count = nextCount + 1;

            return new Tuple<>(executeUnaryOperator(atom.getType(), result.getValue1()), count);
        }

        if (atom.getType() == AtomType.OPEN) {
            int end = start + count;
            int level = 1;
            int i;
            for (i = start + 1; i < end && level > 0; i++) {
                switch (atoms.get(i).getType())
                {
                    case CLOSE:
                        level--;
                        break;
                    case OPEN:
                        level++;
                        break;
                }
            }

            if (level > 0) {
                throw new IllegalArgumentException("Encountered opening bracket without closing one");
            }

            i += 1;

            count = i - start;

            return new Tuple<>(evaluateGroup(start, count), count);
        }

        throw new IllegalArgumentException("Unknown atom '" + atom.getType() + "'");
    }
}

package com.lessmarkup.interfaces.data;

public class OptionalBoolean extends AbstractOptional<Boolean> {

    private OptionalBoolean() {
    }

    private OptionalBoolean(Boolean value) {
        super(value);
    }

    @Override
    public Class<Boolean> getArgumentType() {
        return Boolean.class;
    }

    public static OptionalBoolean of(Boolean value) {
        return new OptionalBoolean(value);
    }

    public static OptionalBoolean OptionalBoolean(boolean value) {
        return new OptionalBoolean(value);
    }

    public static OptionalBoolean empty() {
        return new OptionalBoolean();
    }
}

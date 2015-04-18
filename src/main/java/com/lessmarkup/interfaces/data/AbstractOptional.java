package com.lessmarkup.interfaces.data;

import java.util.NoSuchElementException;
import java.util.Objects;

public abstract class AbstractOptional<T> {
    private final T value;

    protected AbstractOptional() {
        this.value = null;
    }

    protected AbstractOptional(T value) {
        this.value = Objects.requireNonNull(value);
    }

    public T get() {
        if (value == null) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    public boolean isPresent() {
        return value != null;
    }

    public abstract Class<T> getArgumentType();
}

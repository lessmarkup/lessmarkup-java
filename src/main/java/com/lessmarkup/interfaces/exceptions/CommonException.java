package com.lessmarkup.interfaces.exceptions;

public class CommonException extends RuntimeException {
    public CommonException(String message) {
        super(message);
    }
    public CommonException(Throwable e) {
        super(e);
    }
}

package com.softuni.personal_finance_app.exception;

public class FeignCallException extends DomainException{

    public FeignCallException(String message) {
        super(message);
    }

    public FeignCallException(String message, Throwable cause) {
        super(message, cause);
    }
}

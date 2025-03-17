package com.softuni.personal_finance_app.exception;

public class FeignException extends DomainException{

    public FeignException(String message) {
        super(message);
    }

    public FeignException(String message, Throwable cause) {
        super(message, cause);
    }
}

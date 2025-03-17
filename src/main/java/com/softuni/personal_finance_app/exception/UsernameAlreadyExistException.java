package com.softuni.personal_finance_app.exception;

public class UsernameAlreadyExistException extends DomainException {
    public UsernameAlreadyExistException(String message) {
        super(message);
    }

    public UsernameAlreadyExistException(String message, Throwable cause) {
        super(message, cause);
    }
}

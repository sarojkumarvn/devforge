package com.example.devforge.exception;

public class BadRequestException extends DomainException {
    public BadRequestException(String message) {
        super(message); // got the access of the parent class constructor 
        // and add the message to that 
    }
}

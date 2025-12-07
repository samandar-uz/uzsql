package org.example.uzsql.exception;

public class DatabaseAlreadyExistsException extends RuntimeException {

    public DatabaseAlreadyExistsException(String message) {
        super(message);
    }

}

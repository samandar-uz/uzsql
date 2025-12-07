package org.example.uzsql.exception;

public class DatabaseCreationException extends RuntimeException {

    public DatabaseCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}

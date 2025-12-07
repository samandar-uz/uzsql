package org.example.uzsql.exception;

import org.example.uzsql.dto.APIResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DatabaseAlreadyExistsException.class)
    public ResponseEntity<@NotNull APIResponse> handleDbExists(DatabaseAlreadyExistsException e) {
        return ResponseEntity.badRequest().body(
                new APIResponse("error", e.getMessage())
        );
    }

    @ExceptionHandler(DatabaseCreationException.class)
    public ResponseEntity<@NotNull APIResponse> handleDbCreate(DatabaseCreationException e) {
        return ResponseEntity.internalServerError().body(
                new APIResponse("error", e.getMessage())
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<@NotNull APIResponse> handleIllegal(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(
                new APIResponse("error", e.getMessage())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<@NotNull APIResponse> handleAny() {
        return ResponseEntity.internalServerError().body(
                new APIResponse("error", "Server xatosi yuz berdi")
        );
    }
}

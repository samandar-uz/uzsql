package org.example.uzsql.dto;

import lombok.Data;

@Data
public class CreateRequest {
    private String email;
    private String dbName;
    private String dbUser;
    private String recaptchaToken;
}

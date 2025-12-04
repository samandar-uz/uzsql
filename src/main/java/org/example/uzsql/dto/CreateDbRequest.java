package org.example.uzsql.dto;

import lombok.Data;

@Data
public class CreateDbRequest {
    private String dbName;
    private String username;
    private String email;
}

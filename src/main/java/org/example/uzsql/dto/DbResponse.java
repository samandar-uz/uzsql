package org.example.uzsql.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DbResponse {
    private String dbName;
    private String username;
    private String password;
    private String host;
    private Integer port;
}

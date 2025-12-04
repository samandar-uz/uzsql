package org.example.uzsql.dto;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Getter

@Data
public class RecaptchaResponse {
    private boolean success;
    private List<String> errorCodes;
}


package org.example.uzsql.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter

public class RecaptchaResponse {

    private boolean success;

    @JsonProperty("error-codes")
    private List<String> errorCodes;

}

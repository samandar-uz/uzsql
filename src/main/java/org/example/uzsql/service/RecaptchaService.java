package org.example.uzsql.service;


import org.example.uzsql.dto.RecaptchaResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class RecaptchaService {

    @Value("${google.recaptcha.secret}")
    private String recaptchaSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean verify(String token, String userIp) {

        String url = "https://www.google.com/recaptcha/api/siteverify";

        MultiValueMap<@NotNull String, String> params = new LinkedMultiValueMap<>();
        params.add("secret", recaptchaSecret);
        params.add("response", token);
        params.add("remoteip", userIp);

        RecaptchaResponse response = restTemplate.postForObject(url, params, RecaptchaResponse.class);
        if (response == null) return false;
        if (!response.isSuccess()) {
            System.out.println("reCAPTCHA ERROR CODES: " + response.getErrorCodes());
        }

        return response.isSuccess();
    }
}

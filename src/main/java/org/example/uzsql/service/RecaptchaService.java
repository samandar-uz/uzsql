package org.example.uzsql.service;

import org.example.uzsql.dto.RecaptchaResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RecaptchaService {

    @Value("${google.recaptcha.secret}")
    private String recaptchaSecret;

    public boolean verify(String token) {
        String url = String.format(
                "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s",
                recaptchaSecret, token
        );

        RestTemplate restTemplate = new RestTemplate();
        RecaptchaResponse response = restTemplate.getForObject(url, RecaptchaResponse.class);

        return response != null && response.isSuccess();
    }
}

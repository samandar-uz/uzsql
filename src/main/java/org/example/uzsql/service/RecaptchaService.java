package org.example.uzsql.service;

import lombok.extern.slf4j.Slf4j;
import org.example.uzsql.dto.RecaptchaResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class RecaptchaService {

    @Value("${google.recaptcha.secret}")
    private String recaptchaSecret;

    private final RestTemplate restTemplate;
    public RecaptchaService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean verify(String token, String userIp) {

        if (token == null || token.isBlank()) {
            log.warn("reCAPTCHA token bo‘sh keldi");
            return false;
        }

        String url = "https://www.google.com/recaptcha/api/siteverify";

        MultiValueMap<@NotNull String, String> params = new LinkedMultiValueMap<>();
        params.add("secret", recaptchaSecret);
        params.add("response", token);
        params.add("remoteip", userIp);

        try {
            RecaptchaResponse response = restTemplate.postForObject(url, params, RecaptchaResponse.class);
            if (response == null) {
                log.error("reCAPTCHA Google'dan null javob keldi");
                return false;
            }

            if (!response.isSuccess()) {
                log.warn("reCAPTCHA tasdiqlanmadi. Error codes: {}", response.getErrorCodes());
            }

            return response.isSuccess();

        } catch (Exception e) {
            log.error("reCAPTCHA API chaqirishda xatolik", e);
            return false;
        }
    }
}

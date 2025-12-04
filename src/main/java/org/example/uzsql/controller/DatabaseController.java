package org.example.uzsql.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.uzsql.dto.APIResponse;

import org.example.uzsql.dto.CreateRequest;
import org.example.uzsql.service.DatabaseService;
import org.example.uzsql.service.RecaptchaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/create")
@RequiredArgsConstructor
public class DatabaseController {

    private final RecaptchaService recaptchaService;
    private final DatabaseService service;

    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody CreateRequest req,
            HttpServletRequest servletRequest
    ) {
        try {
            String recaptchaToken = req.getRecaptchaToken();
            String userIp = servletRequest.getRemoteAddr();
            boolean captchaVerified = recaptchaService.verify(recaptchaToken, userIp);
            if (!captchaVerified) {
                return ResponseEntity.badRequest().body(
                        new APIResponse("error", "Recaptcha tasdiqlanmadi. Qayta urinib ko'ring.")
                );
            }
            String response = service.createDb(req);
            return ResponseEntity.ok(
                    new APIResponse("success", "MySQL baza yaratildi va " + response + " emailga yuborildi!")
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new APIResponse("error", e.getMessage())
            );
        }
    }


}

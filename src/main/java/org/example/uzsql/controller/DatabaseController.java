package org.example.uzsql.controller;

import lombok.RequiredArgsConstructor;
import org.example.uzsql.dto.ApiResponse;
import org.example.uzsql.dto.CreateDbRequest;
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
    public ResponseEntity<?> create(@RequestBody CreateDbRequest req ,  @RequestParam("g-recaptcha-response") String recaptchaToken) {
        try {

            boolean captchaVerified = recaptchaService.verify(recaptchaToken);

            if (!captchaVerified) {
               return ResponseEntity.badRequest().body(
                       new ApiResponse("error", "Recaptcha tasdiqlanmadi. Iltimos, qayta urinib ko'ring.")
               );
            }

            String response = service.createDb(req);
            return ResponseEntity.ok().body(
                    new ApiResponse("success", "Sizning emailgizni MySQL baza ma'lumotlari yuborildi: " + response)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse("error", e.getMessage())

            );

        }
    }
}

package org.example.uzsql.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.uzsql.dto.APIResponse;
import org.example.uzsql.dto.CreateRequest;
import org.example.uzsql.service.DatabaseService;
import org.example.uzsql.service.RateLimitService;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/create")
@RequiredArgsConstructor
public class DatabaseController {

    private final DatabaseService databaseService;
    private final RateLimitService rateLimit;
    @PostMapping
    public ResponseEntity<@NotNull APIResponse> create(
            @RequestBody CreateRequest req,
            HttpServletRequest request
    ) {
        String userIp = request.getRemoteAddr();



        if (!rateLimit.allow(userIp)) {
            return ResponseEntity.status(429).body(
                    new APIResponse("error", "Kunlik limit tugagan! 24 soatdan keyin urinib ko‘ring.")
            );
        }
        String email = databaseService.createDb(req);

        return ResponseEntity.ok(
                new APIResponse(
                        "success",
                        "MySQL baza yaratildi va " + email + " emailga yuborildi!"
                )
        );
    }
}

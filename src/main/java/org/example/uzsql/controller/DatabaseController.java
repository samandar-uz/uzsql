package org.example.uzsql.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.uzsql.dto.APIResponse;
import org.example.uzsql.dto.CreateRequest;
import org.example.uzsql.service.DatabaseService;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/create")
@RequiredArgsConstructor
public class DatabaseController {

    private final DatabaseService databaseService;

    @PostMapping
    public ResponseEntity<@NotNull APIResponse> create(
            @RequestBody CreateRequest req,
            HttpServletRequest request
    ) {
        String userIp = request.getRemoteAddr();

        String email = databaseService.createDb(req, userIp);

        return ResponseEntity.ok(
                new APIResponse(
                        "success",
                        "MySQL baza yaratildi va " + email + " emailga yuborildi!"
                )
        );
    }
}

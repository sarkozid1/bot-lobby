package com.botlobby.backend.controller;

import com.botlobby.backend.dto.sport.SportResponse;
import com.botlobby.backend.service.SportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Lasd AuthController.java a controller-minta magyarazataert.
// Egyetlen, hitelesitest igenylo (lasd SecurityConfig .anyRequest().authenticated()), de
// egyebkent nagyon egyszeru olvaso vegpont: az osszes elerheto sport listaja.
@RestController
@RequestMapping("/api/sports")
@RequiredArgsConstructor
public class SportController {

    private final SportService sportService;

    @GetMapping
    public List<SportResponse> getAll() {
        return sportService.findAll();
    }
}

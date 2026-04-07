package com.senior.candleShopProject.feature.health.controller;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.feature.health.service.HealthCheckService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "Health check API.")
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthCheckController {

    private final HealthCheckService healthCheckService;

    @GetMapping()
    public ResponseEntity<GenericResponse> health() {

        log.info("Health check endpoint called");
        GenericResponse response = healthCheckService.health();
        return ResponseEntity.ok(response);
    }
}
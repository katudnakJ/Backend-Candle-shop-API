package com.senior.candleShopProject.feature.user.controller;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.feature.user.service.ShopUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@Tag(name = "Candle Shop User API.")
@RequestMapping("v1/user")
@RequiredArgsConstructor
public class UserController {

    private final ShopUserService shopUsersService;

    @GetMapping("/profile/{userId}")
    @Operation(summary = "Get user profile API.")
    public ResponseEntity<GenericResponse> getUserProfile(@PathVariable("userId") String userId) throws ShopServiceApiException {
        log.info("Get user profile by user id {}", userId);
        UUID userUUID = UUID.fromString(userId);

        GenericResponse response = shopUsersService.getUserProfile(userUUID);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
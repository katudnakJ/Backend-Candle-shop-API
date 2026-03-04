package com.senior.candleShopProject.feature.auth.controller;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.feature.auth.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Tag(name = "Login API.")
@RequestMapping("v1/login")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @PostMapping()
    @Operation(summary = "User login API.")
    public ResponseEntity<GenericResponse> login(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                                 HttpServletResponse request) throws ShopServiceApiException {
        log.info("User login with line token {}");

        GenericResponse response = loginService.userLogin(authHeader, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

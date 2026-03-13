package com.senior.candleShopProject.feature.auth.controller.dto;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.feature.auth.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "Logout API.")
@RequestMapping("v1/logout")
@RequiredArgsConstructor
public class LogoutController {

    private final LoginService loginService;

    @PostMapping()
    @Operation(summary = "User logout API.")
    public ResponseEntity<GenericResponse> logout(HttpServletResponse servResp) throws ShopServiceApiException {
        log.info("User logout");
        GenericResponse response =loginService.userLogout(servResp);

        response.setStatus(ResultCode.SUCCESS);
        return ResponseEntity.ok(response);
    }
}

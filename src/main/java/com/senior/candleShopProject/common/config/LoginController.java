package com.senior.candleShopProject.common.config;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.feature.user.service.ShopUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "Candle Shop User API.")
@RequestMapping("v1/login")
@RequiredArgsConstructor
public class LoginController {

    private final ShopUserService shopUserService;

    @PostMapping()
    @Operation(summary = "User login API.")
    public ResponseEntity<GenericResponse> userLogin(@RequestParam(value = "lineToken", required = true) String lineToken) throws ShopServiceApiException {
        log.info("User login with line token {}");

        GenericResponse response = shopUserService.userLogin(lineToken);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

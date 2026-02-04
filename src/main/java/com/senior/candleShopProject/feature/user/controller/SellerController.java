package com.senior.candleShopProject.feature.user.controller;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.feature.user.service.SellerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Tag(name = "Candle Shop User API.")
@RequestMapping("v1/seller")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    @GetMapping("/orders/count/{status}")
    @Operation(summary = "Get order count by status API.")
    public ResponseEntity getSellerOrderCount(@PathVariable(value = "status", required = true) String status) throws ShopServiceApiException {
        log.info("Get seller order count by status {}", status);
        GenericResponse response = sellerService.getSellerOrderCountByStatus(status);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}

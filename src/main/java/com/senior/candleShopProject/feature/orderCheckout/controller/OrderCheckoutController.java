package com.senior.candleShopProject.feature.orderCheckout.controller;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.feature.orderCheckout.controller.dto.request.OrderCheckoutReq;
import com.senior.candleShopProject.feature.orderCheckout.service.OrderCheckoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Generated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@Tag(name = "Candle Shop Checkout API.")
@RequestMapping("v1/checkout")
@RequiredArgsConstructor
public class OrderCheckoutController {

    private static OrderCheckoutService orderCheckoutService;

    @PostMapping()
    @Operation(summary = "Checkout order API.")
    public ResponseEntity checkoutOrder(@RequestAttribute("userId") String userId,
                                        @RequestPart("imageData") MultipartFile imageData,
                                        @RequestBody OrderCheckoutReq orderCheckoutReq) throws ShopServiceApiException, IOException {
        log.info("Checking out order API for user {}", userId);

        UUID userUuid = UUID.fromString(userId);
        GenericResponse response = orderCheckoutService.checkoutOrder(userUuid, imageData, orderCheckoutReq);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}")
    public ResponseEntity retryPayment(@RequestAttribute("userId") String userId,
                                        @PathVariable("orderId") String orderId,
                                        @RequestPart("imageData") MultipartFile imageData) throws ShopServiceApiException, IOException {
        log.info("Checking out order API for user {}", userId);

        UUID userUuid = UUID.fromString(userId);
        UUID orderUuid = UUID.fromString(orderId);
        GenericResponse response = orderCheckoutService.retryPayment(userUuid, orderUuid, imageData);
        return ResponseEntity.ok(response);
    }
}

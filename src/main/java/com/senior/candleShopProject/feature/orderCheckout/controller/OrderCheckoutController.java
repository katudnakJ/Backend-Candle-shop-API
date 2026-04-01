package com.senior.candleShopProject.feature.orderCheckout.controller;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.feature.orderCheckout.service.OrderCheckoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@Tag(name = "Checkout API.")
@RequestMapping("v1/checkout")
@RequiredArgsConstructor
public class OrderCheckoutController {

    private final OrderCheckoutService orderCheckoutService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Checkout order API.")
    @PreAuthorize("hasRole('CUST') or hasRole('DEVELOPER')")
    public ResponseEntity checkoutOrder(@RequestAttribute("userId") String userId,
                                        @RequestParam("image_data") MultipartFile imageData,
                                        @RequestParam("shopping_cart_item_ids") List<String> shoppingCartItemIds,
                                        @RequestParam("address_id") String addressId
    ) throws ShopServiceApiException, IOException {
        log.info("Checking out order API for user {}", userId);

        UUID userUuid = UUID.fromString(userId);
        UUID addressUuid = UUID.fromString(addressId);
        GenericResponse response = orderCheckoutService.checkoutOrder(userUuid, imageData, shoppingCartItemIds,addressUuid);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{orderId}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Retry payment for order API.", description = "Retry payment when payment is rejected.")
    @PreAuthorize("hasRole('CUST') or hasRole('DEVELOPER')")
    public ResponseEntity retryPayment(@RequestAttribute("userId") String userId,
                                        @PathVariable("orderId") String orderId,
                                        @RequestParam("image_data") MultipartFile imageData) throws ShopServiceApiException, IOException {
        log.info("Checking out order API for user {}", userId);

        UUID userUuid = UUID.fromString(userId);
        UUID orderUuid = UUID.fromString(orderId);
        GenericResponse response = orderCheckoutService.retryPayment(userUuid, orderUuid, imageData);
        return ResponseEntity.ok(response);
    }
}

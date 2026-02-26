package com.senior.candleShopProject.feature.order.controller;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.feature.order.service.OrderService;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@Tag(name = "Candle Shop Order Service API.")
@RequestMapping("v1/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Schema(description = "Get all carriers.")
    @GetMapping("/carriers")
    public ResponseEntity<GenericResponse> getAllCarriers(@RequestAttribute("userId") String userId) {
        log.info("Getting all carriers");

        GenericResponse response = orderService.getAllCarriers();
        return ResponseEntity.ok(response);
    }

    @Schema(description = "Get order by status.")
    @GetMapping()
    public ResponseEntity<GenericResponse> getOrderByStatus(@RequestAttribute("userId") String userId,
                                                            @RequestParam("status") String status) throws ShopServiceApiException {
        log.info("Getting order by status for status: {}", status);
        UUID userUUID = UUID.fromString(userId);

        GenericResponse response = orderService.getOrderByStatus(userUUID, status);
        return ResponseEntity.ok(response);
    }


}

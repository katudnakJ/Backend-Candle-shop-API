package com.senior.candleShopProject.feature.order.controller;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.feature.order.controller.dto.request.RejectPaymentReq;
import com.senior.candleShopProject.feature.order.controller.dto.request.TrackOrderReq;
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

    @Schema(description = "Get order details by order id.")
    @GetMapping("/{orderId}")
    public ResponseEntity<GenericResponse> getOrderDetailsByOrderId(@RequestAttribute("userId") String userId,
                                                                   @PathVariable("orderId") String orderId) throws ShopServiceApiException {
        log.info("Getting order details by order id: {}", orderId);
        UUID userUUID = UUID.fromString(userId);
        UUID orderUUID = UUID.fromString(orderId);

        GenericResponse response = orderService.getOrderDetailsByOrderId(userUUID, orderUUID);
        return ResponseEntity.ok(response);
    }

    @Schema (description = "Confirm payment for order by seller.")
    @PatchMapping("/{orderId}/confirm")
    public ResponseEntity confirmPayment(@RequestAttribute("userId") String userId,
                                         @PathVariable("orderId") String orderId) throws ShopServiceApiException {
        log.info("Confirming payment for order {}", orderId);
        UUID userUUID = UUID.fromString(userId);
        UUID orderUUID = UUID.fromString(orderId);

        GenericResponse response = orderService.confirmPayment(userUUID, orderUUID);
        return ResponseEntity.ok(response);
    }

    @Schema (description = "Reject payment for order by seller.")
    @PatchMapping("/{orderId}/reject")
    public ResponseEntity rejectPayment(@RequestAttribute("userId") String userId,
                                        @PathVariable ("orderId") String orderId,
                                        @RequestBody RejectPaymentReq rejectPaymentReq) throws ShopServiceApiException {
            log.info("Rejecting payment for order {}", orderId);
            UUID userUUID = UUID.fromString(userId);
            UUID orderUUID = UUID.fromString(orderId);

            GenericResponse response = orderService.rejectPayment(userUUID, orderUUID, rejectPaymentReq);
            return ResponseEntity.ok(response);
    }

    @Schema(description = "Track order by seller.")
    @PatchMapping("/{orderId}/track")
    public ResponseEntity trackOrder(@RequestAttribute("userId") String userId,
                                     @PathVariable ("orderId") String orderId,
                                     @RequestBody TrackOrderReq trackOrderReq) throws ShopServiceApiException {
        log.info("Tracking order {}",orderId);
        UUID userUUID = UUID.fromString(userId);
        UUID orderUUID = UUID.fromString(orderId);

        GenericResponse response = orderService.trackOrder(userUUID,orderUUID, trackOrderReq);
        return ResponseEntity.ok(response);
    }
}

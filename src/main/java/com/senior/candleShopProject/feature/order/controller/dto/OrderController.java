package com.senior.candleShopProject.feature.order.controller.dto;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.feature.order.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "Candle Shop Order Service API.")
@RequestMapping("v1/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/carriers")
    public ResponseEntity<GenericResponse> getAllCarriers(@RequestAttribute("userId") String userId) {
        log.info("Getting all carriers");

        GenericResponse response = orderService.getAllCarriers();
        return ResponseEntity.ok(response);
    }
}

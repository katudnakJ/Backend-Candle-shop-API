package com.senior.candleShopProject.feature.orderCheckout.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "Candle Shop Checkout API.")
@RequestMapping("v1/checkout")
@RequiredArgsConstructor
public class OrderCheckoutController {
}

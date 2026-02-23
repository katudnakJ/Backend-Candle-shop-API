package com.senior.candleShopProject.feature.orderCheckout.controller.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrdersReq {
    private int totalQuantity;
    private BigDecimal totalAmount;
    private BigDecimal netAmount;
    private String orderCreatedDate;
}
package com.senior.candleShopProject.feature.order.controller.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class OrderItemsListResp {
    private UUID orderItemId;
    private String productName;
    private Integer quantity;
    private BigDecimal pricePerUnit;
    private BigDecimal subTotal;
    private String productImagePath;
}

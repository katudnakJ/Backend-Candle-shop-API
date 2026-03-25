package com.senior.candleShopProject.feature.order.controller.dto.response.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class OrderByStatusMappingResp {
    private UUID orderId;
    private Integer totalQuantity;
    private BigDecimal totalAmount;
    private BigDecimal netAmount;
    private String orderStatus;
    private Instant orderCreatedAt;
    private String paymentStatus;
    private String orderNo;
    private String addressLabel;
    private List<String> trackingNo;
    private String rejectionReason;
    private String deliveryMethod;
    private List<OrderItemsListResp> orderItems;
}

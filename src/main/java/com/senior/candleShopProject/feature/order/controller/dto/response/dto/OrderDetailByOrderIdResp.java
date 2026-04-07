package com.senior.candleShopProject.feature.order.controller.dto.response.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class OrderDetailByOrderIdResp {
    private UUID orderId;
    private Integer totalQuantity;
    private BigDecimal totalAmount;
    private BigDecimal netAmount;
    private String orderStatus;
    private String orderNo;
    private String addressLabel;
    private String deliveryAddress;
    private String postcode;
    private String province;
    private String district;
    private String subDistrict;
    private String recipientFirstName;
    private String recipientLastName;
    private String recipientPhone;
    private Instant paymentCreatedAt;
    private Instant paymentApproveAt;
    private Instant orderCreatedAt;
    private Instant completedAt;
    private List<String> trackingNumber;
    private String deliveryMethod;
    private String rejectionReason;
}

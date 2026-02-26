package com.senior.candleShopProject.feature.order.controller.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class RejectPaymentReq {
    private UUID orderId;
    private String reason;
}

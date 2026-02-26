package com.senior.candleShopProject.feature.order.controller.dto.response.dto;

import com.senior.candleShopProject.common.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
public class OrderStatusChangeResp {
    private UUID orderId;
    private OrderStatus newStatus;
    private Instant statusChangedAt;
}

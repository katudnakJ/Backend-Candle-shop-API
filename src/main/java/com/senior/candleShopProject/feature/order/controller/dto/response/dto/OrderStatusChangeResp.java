package com.senior.candleShopProject.feature.order.controller.dto.response.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.senior.candleShopProject.common.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
public class OrderStatusChangeResp {

    @JsonProperty("order_id")
    private UUID orderId;

    @JsonProperty("new_status")
    private String newStatus;

    @JsonProperty("status_changed_at")
    private Instant statusChangedAt;
}

package com.senior.candleShopProject.feature.order.controller.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class TrackOrderReq {
    private UUID orderId;
    private String newStatus;
    private List<String> trackingNumber;
}

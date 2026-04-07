package com.senior.candleShopProject.feature.order.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class TrackOrderReq {
    @JsonProperty("tracking_number")
    private List<String> trackingNumber;
}

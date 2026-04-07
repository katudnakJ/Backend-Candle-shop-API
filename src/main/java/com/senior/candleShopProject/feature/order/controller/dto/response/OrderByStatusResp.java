package com.senior.candleShopProject.feature.order.controller.dto.response;

import com.senior.candleShopProject.feature.order.controller.dto.response.dto.OrderByStatusMappingResp;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderByStatusResp {
    private List<OrderByStatusMappingResp> orders;
    private int page;
    private int size;
    private int startAt;
    private int endAt;
    private Long totalOrders;
    private boolean hasNext;
}

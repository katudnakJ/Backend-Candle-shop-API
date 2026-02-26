package com.senior.candleShopProject.feature.order.controller.dto.response.dto;

import com.senior.candleShopProject.common.OrderStatus;
import com.senior.candleShopProject.feature.order.controller.dto.response.OrderItemsListResp;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class OrderByStatusResp {
    private UUID orderId;
    private Integer totalQuantity;
    private BigDecimal totalAmount;
    private BigDecimal netAmount;
    private OrderStatus orderStatus;
    private String orderNo;
    private String addressLabel;
    private String trackingNo;
    private String rejectionReason;
    private List<OrderItemsListResp> orderItems;
}

package com.senior.candleShopProject.feature.order.controller.dto.response;

import com.senior.candleShopProject.datasource.domain.orders.IOrderByStatusResp;
import com.senior.candleShopProject.datasource.domain.orders.IOrderItemListResp;
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
    private String orderStatus;
    private String orderNo;
    private String addressLabel;
    private List<OrderItemsListResp> orderItems;
}

package com.senior.candleShopProject.feature.order.controller.dto.response.dto;

import com.senior.candleShopProject.datasource.domain.orders.IOrderItemListResp;
import com.senior.candleShopProject.feature.order.controller.dto.response.OrderDetailByOrderIdResp;
import com.senior.candleShopProject.feature.order.controller.dto.response.OrderItemsListResp;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class OrderDetailsResp {
    OrderDetailByOrderIdResp orderDetail;
    List<OrderItemsListResp> orderItems;
}


package com.senior.candleShopProject.feature.order.controller.dto.response;

import com.senior.candleShopProject.feature.order.controller.dto.response.dto.OrderDetailByOrderIdResp;
import com.senior.candleShopProject.feature.order.controller.dto.response.dto.OrderItemsListResp;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class OrderDetailsResp {
    OrderDetailByOrderIdResp orderDetail;
    List<OrderItemsListResp> orderItems;
}


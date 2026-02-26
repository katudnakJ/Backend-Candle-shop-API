package com.senior.candleShopProject.feature.order.controller.dto.response;

import com.senior.candleShopProject.datasource.domain.orders.IOrderDetailByStatusResp;
import com.senior.candleShopProject.datasource.domain.orders.IOrderItemListResp;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class OrderDetailsResp {
    IOrderDetailByStatusResp orderDetail;
    List<IOrderItemListResp> orderItems;
}


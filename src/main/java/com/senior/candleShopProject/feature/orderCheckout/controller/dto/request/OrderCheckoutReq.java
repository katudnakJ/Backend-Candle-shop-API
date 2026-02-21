package com.senior.candleShopProject.feature.orderCheckout.controller.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OrderCheckoutReq {
    private List<String> shoppingCartItemIds = new ArrayList<>();
    private OrdersReq ordersReq;
}

package com.senior.candleShopProject.datasource.domain.orders;

public interface IReceiptOrderItemResp {
    String getProductName();
    int getQuantity();
    String getPricePerUnit();
    String getSubTotalPrice();
}

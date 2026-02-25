package com.senior.candleShopProject.datasource.domain.orders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface IOrderDetailByStatusResp {
    UUID getOrderId();
    Integer totalQuantity();
    BigDecimal totalAmount();
    BigDecimal netAmount();
    String getOrderStatus();
    String getOrderNo();
    String getAddressLabel();
    String getDeliveryAddress();
    String getPostcode();
    String getProvince();
    String getDistrict();
    String getSubDistrict();
    String getRecipientFirstName();
    String getRecipientLastName();
    String getRecipientPhone();
    Instant getPaymentCreatedAt();
    Instant getOrderCreatedAt();
    Instant getOrderCompletedAt();
}

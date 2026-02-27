package com.senior.candleShopProject.datasource.domain.orders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface IOrderDetailByOrderIdResp {
    UUID getOrderId();
    Integer getTotalQuantity();
    BigDecimal getTotalAmount();
    BigDecimal getNetAmount();
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
    Instant getPaymentApproveAt();
    Instant getOrderCreatedAt();
    Instant getCompletedAt();
    String getTrackingNumber();
    String getDeliveryMethod();
    String getRejectionReason();
}

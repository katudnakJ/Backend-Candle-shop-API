package com.senior.candleShopProject.datasource.domain.orders;

import java.math.BigDecimal;
import java.util.UUID;

public interface IOrderByStatusResp {
    UUID getOrderId();
    Integer getTotalQuantity();
    BigDecimal getTotalAmount();
    BigDecimal getNetAmount();
    String getOrderStatus();
    String getOrderNo();
    String getAddressLabel();
    String getTrackingNumber();
    String getDeliveryMethod();
    String getRejectionReason();
}
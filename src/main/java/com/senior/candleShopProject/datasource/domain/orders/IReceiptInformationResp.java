package com.senior.candleShopProject.datasource.domain.orders;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


public interface IReceiptInformationResp {

//    Payment information
    UUID getPaymentId();
    String getPaymentReceiptNumber();

//    Order information
    String getOrderNumber();
    BigDecimal getOrderTotalAmount();
    BigDecimal getOrderNetAmount();
    String getOrderStatus();

//    Seller information
    String getSellerFirstName(); // seller's recipientFirstName
    String getSellerLastName(); // seller's recipientLastName
    String getSellerPhone(); // seller's recipientPhone
    String getSellerDeliveryAddress(); // seller's address
    String getSellerPostcode();
    String getSellerProvince();
    String getSellerDistrict();
    String getSellerSubDistrict();

//      customer information
    UUID getCustomerId();
    String getCustomerFirstName(); // customer's recipientFirstName
    String getCustomerLastName(); // customer's recipientLastName
    String getCustomerPhone(); // customer's recipientPhone
    String getCustomerDeliveryAddress();
    String getCustomerPostcode();
    String getCustomerProvince();
    String getCustomerDistrict();
    String getCustomerSubDistrict();

}

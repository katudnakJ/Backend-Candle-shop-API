package com.senior.candleShopProject.datasource.domain;

public interface IAddressResp {
    String getAddressId();
    String getDeliveryAddress();
    String getPostcode();
    String getProvince();
    String getDistrict();
    String getSubDistrict();
    String getAddressLabel();
    boolean getIsDefault();
    String getRecipientFirstName();
    String getRecipientLastName();
    String getRecipientPhone();
}

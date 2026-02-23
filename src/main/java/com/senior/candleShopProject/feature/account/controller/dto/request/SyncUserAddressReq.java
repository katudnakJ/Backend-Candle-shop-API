package com.senior.candleShopProject.feature.account.controller.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SyncUserAddressReq {
    private String addressId;
    private String deliveryAddress;
    private String postcode;
    private String province;
    private String district;
    private String subDistrict;
    private String addressLabel;
    private Boolean isDefault;
    private String recipientFirstName;
    private String recipientLastName;
    private String recipientPhone;
}

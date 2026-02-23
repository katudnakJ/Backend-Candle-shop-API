package com.senior.candleShopProject.feature.account.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AddUserAddressReq {
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

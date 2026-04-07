package com.senior.candleShopProject.feature.account.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddUserAddressReq {
    @JsonProperty("delivery_address")
    private String deliveryAddress;

    @JsonProperty("postcode")
    private String postcode;

    @JsonProperty("province")
    private String province;

    @JsonProperty("district")
    private String district;

    @JsonProperty("sub_district")
    private String subDistrict;

    @JsonProperty("address_label")
    private String addressLabel;

    @JsonProperty("is_default")
    private Boolean isDefault;

    @JsonProperty("recipient_first_name")
    private String recipientFirstName;

    @JsonProperty("recipient_last_name")
    private String recipientLastName;

    @JsonProperty("recipient_phone")
    private String recipientPhone;
}

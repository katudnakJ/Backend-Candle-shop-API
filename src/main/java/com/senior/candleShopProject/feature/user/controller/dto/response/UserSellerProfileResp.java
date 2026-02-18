package com.senior.candleShopProject.feature.user.controller.dto.response;

import com.senior.candleShopProject.datasource.domain.IAddressResp;
import com.senior.candleShopProject.datasource.domain.IUsersResp;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserSellerProfileResp {
    private Boolean isSeller;
    private String bankQrPaymentImgPath;
    private List<IAddressResp> address;
}
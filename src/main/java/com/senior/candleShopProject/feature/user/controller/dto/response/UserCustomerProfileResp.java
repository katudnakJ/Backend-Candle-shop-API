package com.senior.candleShopProject.feature.user.controller.dto.response;

import com.senior.candleShopProject.datasource.domain.IAddressResp;
import com.senior.candleShopProject.datasource.domain.IUsersResp;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserCustomerProfileResp {
    Boolean isSeller;
    List<IAddressResp> address;
}

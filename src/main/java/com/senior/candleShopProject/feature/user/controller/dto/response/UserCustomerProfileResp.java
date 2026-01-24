package com.senior.candleShopProject.feature.user.controller.dto.response;

import com.senior.candleShopProject.datasource.domain.IUsersResp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCustomerProfileResp {
    IUsersResp userProfile;
}

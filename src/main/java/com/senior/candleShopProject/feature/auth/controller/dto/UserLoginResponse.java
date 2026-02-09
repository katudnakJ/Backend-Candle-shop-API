package com.senior.candleShopProject.feature.auth.controller.dto;

import com.senior.candleShopProject.common.LineService.dto.LineProfileData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginResponse {
    String userId;
    String userRole;
    LineProfileData lineProfile = new LineProfileData();
}

package com.senior.candleShopProject.feature.auth.controller.dto;

import com.senior.candleShopProject.common.LineService.dto.LineProfileData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginResponse {
    private String userId;
    private String userRole;
    private LineProfileData lineProfile = new LineProfileData();
}

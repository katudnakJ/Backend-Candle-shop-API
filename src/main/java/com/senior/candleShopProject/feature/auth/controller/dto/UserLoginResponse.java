package com.senior.candleShopProject.feature.auth.controller.dto;

import com.senior.candleShopProject.common.LineService.dto.LineProfileResp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginResponse {
    LineProfileResp lineProfile = new LineProfileResp();
}

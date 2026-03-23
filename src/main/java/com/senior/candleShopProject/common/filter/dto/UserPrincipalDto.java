package com.senior.candleShopProject.common.filter.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPrincipalDto {
    private String userId;
    private String role;
}

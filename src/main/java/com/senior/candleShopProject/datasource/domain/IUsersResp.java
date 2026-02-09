package com.senior.candleShopProject.datasource.domain;


import java.util.UUID;

public interface IUsersResp {
    UUID getUserId();
    Boolean getIsSeller();
    String getUserRole();

}

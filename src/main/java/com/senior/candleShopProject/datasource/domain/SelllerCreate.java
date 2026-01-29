package com.senior.candleShopProject.datasource.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SelllerCreate {
    UUID sellerId;
    UUID userId;
    String SellerFirstName;
    String SellerLastName;
    String SellerPhoneNumber;
    String BankQrPaymentImgSlug;
}

package com.senior.candleShopProject.datasource.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
public class IProductHomeListItemResp {
    private UUID productId;
    private String productName;
    private BigDecimal price;
    private Boolean isActive;
    private Instant productCreatedDate;
    private Integer totalSelled;
    private String productImgPath;
}

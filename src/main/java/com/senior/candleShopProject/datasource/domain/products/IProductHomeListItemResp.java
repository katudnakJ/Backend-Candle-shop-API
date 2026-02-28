package com.senior.candleShopProject.datasource.domain.products;

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
    private Instant productCreatedDate;
    private Integer totalSelled;
    private String productSlug;
    private String productImgPath;
}

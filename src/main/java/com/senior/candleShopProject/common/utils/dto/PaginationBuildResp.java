package com.senior.candleShopProject.common.utils.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PaginationBuildResp {
    private int startAt;
    private int endAt;
    private Long totalItems;
    private boolean hasNext;
}

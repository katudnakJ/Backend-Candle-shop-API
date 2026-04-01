package com.senior.candleShopProject.common.utils.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Data
@RequiredArgsConstructor
public class RangeOfMonthResp {
    private Instant startDate;
    private Instant endDate;
}

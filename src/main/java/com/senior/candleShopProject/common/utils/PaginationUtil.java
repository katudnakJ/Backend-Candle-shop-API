package com.senior.candleShopProject.common.utils;

import com.senior.candleShopProject.common.utils.dto.PaginationBuildResp;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class PaginationUtil {

    public PaginationBuildResp buildPagination(int page, int size, Long totalCounts) {

        if (totalCounts == 0) {
            return new PaginationBuildResp(0, 0, 0L, false);
        }

        int startAt = (page * size) + 1;
        int endAt = Math.min((page + 1) * size, totalCounts.intValue());
        boolean hasNext = endAt < totalCounts;

        return new PaginationBuildResp(startAt, endAt, totalCounts, hasNext);
    }
}

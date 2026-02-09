package com.senior.candleShopProject.common.utils;

import com.senior.candleShopProject.datasource.repo.OrdersRepo;
import org.springframework.stereotype.Component;

@Component
public class RunningNumberGenerator {

    private OrdersRepo ordersRepo;

    public String generateOrderNo(Long sequenceNumber) {
        String prefix = Constants.PREFIX_ORDER_NO;
        Long orderNoSeq = ordersRepo.getNextOrderNo();
        return prefix + orderNoSeq;
    }
}

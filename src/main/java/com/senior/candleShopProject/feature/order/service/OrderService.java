package com.senior.candleShopProject.feature.order.service;

import com.senior.candleShopProject.datasource.repo.OrdersRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrdersRepo ordersRepo;

}

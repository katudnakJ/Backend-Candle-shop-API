package com.senior.candleShopProject.feature.order.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.datasource.repo.CarriersRepo;
import com.senior.candleShopProject.datasource.repo.OrdersRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrdersRepo ordersRepo;
    private final CarriersRepo carriersRepo;

    public GenericResponse getAllCarriers() {
        GenericResponse response = new GenericResponse();
        response.setData(carriersRepo.findAll());
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }
}

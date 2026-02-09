package com.senior.candleShopProject.feature.user.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.OrderStatus;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopInvalidParamException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.datasource.repo.OrdersRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SellerService {

    private final OrdersRepo ordersRepo;

    public GenericResponse getSellerOrderCountByStatus(String status) throws ShopServiceApiException {

        if(!OrderStatus.isValidStatus(status))
            throw new ShopInvalidParamException(ResultCode.INVALID_PARAMS, "Invalid order status.");

        Integer orderCount = ordersRepo.getCountOrdersWithStatus(status);

        GenericResponse response = new GenericResponse();
        response.setData(orderCount);
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }
}

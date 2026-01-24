package com.senior.candleShopProject.feature.user.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopDataNotFoundException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.datasource.domain.IUsersResp;
import com.senior.candleShopProject.datasource.repo.OrdersRepo;
import com.senior.candleShopProject.datasource.repo.UsersRepo;
import com.senior.candleShopProject.feature.user.controller.dto.response.UserCustomerProfileResp;
import com.senior.candleShopProject.feature.user.controller.dto.response.UserSellerProfileResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopUserService {

    private final UsersRepo usersRepo;
    private final OrdersRepo ordersRepo;

    public GenericResponse getUserProfile(UUID sellerId) throws ShopServiceApiException {
        IUsersResp userProfile = usersRepo.getUserProfile(sellerId);
        if (userProfile == null)
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "User profile not found.");

        GenericResponse response = new GenericResponse();

        if (userProfile.getIsSeller()){
            response.setData(getUserSellerProfile(userProfile));
        }else{
            UserCustomerProfileResp customerProfileResp = new UserCustomerProfileResp();
            customerProfileResp.setUserProfile(userProfile);
            response.setData(customerProfileResp);
        }

        response.setStatus(ResultCode.SUCCESS);
        return response;
    }

    private UserSellerProfileResp getUserSellerProfile(IUsersResp userProfile) {
        Integer orderPdCount = ordersRepo.getCountOrdersWithStatusPD();

        UserSellerProfileResp userProfileResp = new UserSellerProfileResp();
        userProfileResp.setUser_profile(userProfile);
        userProfileResp.setOrderPdCount(orderPdCount);
        return userProfileResp;
    }
}

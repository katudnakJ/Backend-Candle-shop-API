package com.senior.candleShopProject.feature.user.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.OrderStatus;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopDataNotFoundException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.common.utils.Constants;
import com.senior.candleShopProject.common.utils.JwtUtils;
import com.senior.candleShopProject.datasource.domain.IUsersResp;
import com.senior.candleShopProject.datasource.domain.UserCreate;
import com.senior.candleShopProject.datasource.entities.UsersEntity;
import com.senior.candleShopProject.datasource.repo.OrdersRepo;
import com.senior.candleShopProject.datasource.repo.UsersRepo;
import com.senior.candleShopProject.feature.user.controller.dto.response.UserCustomerProfileResp;
import com.senior.candleShopProject.feature.user.controller.dto.response.UserLoginResponse;
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
    private final JwtUtils jwtUtils;

    public GenericResponse userLogin(String lineToken) throws ShopServiceApiException {
//       String lineTokenVerifyUrl = Constants.LINE_TOKEN_VERIFY_URL + "?id_token=" + lineToken;

        String lineUserId = "lintestid12345"; //test
        IUsersResp userProfile = usersRepo.getUserProfileByLineId(lineUserId);

        if (userProfile == null) {
            UsersEntity newUser = new UsersEntity();
            newUser.setLineId("lintestid12345");
            newUser.setLineDisplayName("Test User");
            newUser.setIsSeller(false);
            newUser.setUserRole(Constants.ROLE_CUSTOMER);
            usersRepo.save(newUser);

            System.out.println("Create new user with line id: " + lineUserId);
        }else{
            System.out.println("User login with line id: " + lineUserId);
        }

        userProfile = usersRepo.getUserProfileByLineId(lineUserId);
        String token = jwtUtils.generateToken(userProfile.getUserId(), userProfile.getUserRole());

        UserLoginResponse userLoginResponse = new UserLoginResponse();
        userLoginResponse.setToken(token);

        GenericResponse response = new GenericResponse();
        response.setData(userLoginResponse);
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }

    public GenericResponse getUserProfile(UUID sellerId) throws ShopServiceApiException {
        IUsersResp userProfile = usersRepo.getUserProfile(sellerId);
        if (userProfile == null)
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "User profile not found.");

        GenericResponse response = new GenericResponse();

        if (userProfile.getIsSeller()){
            UserSellerProfileResp userProfileResp = new UserSellerProfileResp();
            Integer orderPdCount = getCountOrderByStatus(OrderStatus.ORDER_PAYMENT_PENDING.getOrderStatusCode());
            userProfileResp.setUser_profile(userProfile);
            userProfileResp.setOrderPdCount(orderPdCount);

            response.setData(userProfileResp);
        }else{
            UserCustomerProfileResp customerProfileResp = new UserCustomerProfileResp();
            customerProfileResp.setUserProfile(userProfile);

            response.setData(customerProfileResp);
        }
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }
    private Integer getCountOrderByStatus(String status) {
        Integer orderCount = ordersRepo.getCountOrdersWithStatus(status);
        return orderCount;
    }
}
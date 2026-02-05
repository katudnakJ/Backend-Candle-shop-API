package com.senior.candleShopProject.feature.auth.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.LineService.LineLoginService;
import com.senior.candleShopProject.common.LineService.dto.LineProfileResp;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopInvalidParamException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.common.exception.ShopUnAuthorizedException;
import com.senior.candleShopProject.common.utils.Constants;
import com.senior.candleShopProject.common.utils.JwtUtils;
import com.senior.candleShopProject.datasource.domain.IUsersResp;
import com.senior.candleShopProject.datasource.entities.CustomersEntity;
import com.senior.candleShopProject.datasource.entities.UsersEntity;
import com.senior.candleShopProject.datasource.repo.UsersRepo;
import com.senior.candleShopProject.feature.auth.controller.dto.UserLoginResponse;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final JwtUtils jwtUtils;

    private final LineLoginService lineLoginService;

    private final UsersRepo usersRepo;

    public GenericResponse userLogin(String authHeader) throws ShopServiceApiException {
//        LineService
        LineProfileResp lineProfileResp;
        try{
            lineProfileResp = lineLoginService.getLineProfile(authHeader);
        } catch (Exception exception){
            log.error("Error get Line Profile (LineLoginService.getLineProfile()): {}", exception.getMessage());
            throw new ShopUnAuthorizedException(ResultCode.UNAUTHORIZED);
        }

        String lineUserId = lineProfileResp.getUserId();

        IUsersResp userProfile = usersRepo.getUserProfileByLineId(lineUserId);

        if (userProfile == null) {
            CustomersEntity customersEntity = new CustomersEntity();

            UsersEntity newUser = new UsersEntity();
            newUser.setLineId(lineUserId);
            newUser.setIsSeller(false);
            newUser.setUserRole(Constants.ROLE_CUSTOMER);
            newUser.setCustomersEntity(customersEntity);

            customersEntity.setUsersEntity(newUser);
            usersRepo.save(newUser);
        }

        userProfile = usersRepo.getUserProfileByLineId(lineUserId);
        UUID userId = userProfile.getUserId();
        String token = jwtUtils.generateToken(userId, userProfile.getUserRole());

        UserLoginResponse userLoginResponse = new UserLoginResponse();
        userLoginResponse.setToken( Constants.TOKEN_PREFIX + token);
        lineProfileResp.setUserId(userId.toString());
        userLoginResponse.setLineProfile(lineProfileResp);

        GenericResponse response = new GenericResponse();
        response.setData(userLoginResponse);
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }
}

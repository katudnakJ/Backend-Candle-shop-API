package com.senior.candleShopProject.feature.auth.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopInvalidParamException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final JwtUtils jwtUtils;
    private final UsersRepo usersRepo;

    public GenericResponse userLogin(String lineToken) throws ShopServiceApiException {
//       String lineTokenVerifyUrl = Constants.LINE_TOKEN_VERIFY_URL + "?id_token=" + lineToken;
        if (StringUtils.isEmpty(lineToken))
            throw new ShopInvalidParamException(ResultCode.INVALID_PARAMS, "Line token is required.");

        String lineUserId = "lintestid12345"; //test
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
        String token = jwtUtils.generateToken(userProfile.getUserId(), userProfile.getUserRole());

        UserLoginResponse userLoginResponse = new UserLoginResponse();
        userLoginResponse.setToken(token);

        GenericResponse response = new GenericResponse();
        response.setData(userLoginResponse);
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }
}

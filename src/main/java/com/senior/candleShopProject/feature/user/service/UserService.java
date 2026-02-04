package com.senior.candleShopProject.feature.user.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopDataNotFoundException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.datasource.domain.IAddressResp;
import com.senior.candleShopProject.datasource.domain.IUsersResp;
import com.senior.candleShopProject.datasource.repo.AddressesRepo;
import com.senior.candleShopProject.datasource.repo.SellerRepo;
import com.senior.candleShopProject.datasource.repo.UsersRepo;
import com.senior.candleShopProject.feature.user.controller.dto.response.UserCustomerProfileResp;
import com.senior.candleShopProject.feature.user.controller.dto.response.UserSellerProfileResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${app.storage.public-image-base-url}")
    private String publicBaseImgUrl;

    private final UsersRepo usersRepo;
    private final SellerRepo sellerRepo;
    private final AddressesRepo addressesRepo;

    public GenericResponse getUserProfile(UUID userId) throws ShopServiceApiException {
        IUsersResp userProfile = usersRepo.getUserProfile(userId);

        if (userProfile == null)
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "User profile not found.");

        GenericResponse response = new GenericResponse();

        if (userProfile.getIsSeller()){
            UserSellerProfileResp sellerProfileResp = new UserSellerProfileResp();
            sellerProfileResp.setIsSeller(true);

            String bankQrPaymentImgPath = sellerRepo.getBankQrPaymentImgPathByUserId(userId);
            sellerProfileResp.setBankQrPaymentImgPath(publicBaseImgUrl + bankQrPaymentImgPath);

            List<IAddressResp> userAddresses = addressesRepo.findAddressesByUsersId(userId);
            sellerProfileResp.setAddress(userAddresses);

            response.setData(sellerProfileResp);
        }else{
            UserCustomerProfileResp customerProfileResp = new UserCustomerProfileResp();
            customerProfileResp.setIsSeller(false);

            List<IAddressResp> userAddresses = addressesRepo.findAddressesByUsersId(userId);
            customerProfileResp.setAddress(userAddresses);

            response.setData(customerProfileResp);
        }
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }
}
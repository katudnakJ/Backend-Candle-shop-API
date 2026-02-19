package com.senior.candleShopProject.feature.account.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.*;
import com.senior.candleShopProject.common.utils.CustomizeResponseUtil;
import com.senior.candleShopProject.datasource.domain.IAddressResp;
import com.senior.candleShopProject.datasource.domain.IUsersResp;
import com.senior.candleShopProject.datasource.entities.AddressesEntity;
import com.senior.candleShopProject.datasource.entities.UsersEntity;
import com.senior.candleShopProject.datasource.repo.AddressesRepo;
import com.senior.candleShopProject.datasource.repo.UsersRepo;
import com.senior.candleShopProject.feature.account.controller.dto.request.AddUserAddressReq;
import com.senior.candleShopProject.feature.account.controller.dto.request.SyncUserAddressReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    @Value("${app.storage.public-image-base-url}")
    private String publicBaseImgUrl;

    private final UsersRepo usersRepo;
    private final AddressesRepo addressesRepo;

    public GenericResponse getUserAddresses(UUID userId) throws ShopServiceApiException {
        IUsersResp userProfile = usersRepo.getUserProfile(userId);

        if (userProfile == null)
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "User not found.");

        List<IAddressResp> addressList = addressesRepo.findAddressesEntitiesByUsersEntity_UserId(userId);

        GenericResponse response = new GenericResponse();
        response.setData(addressList.isEmpty() ? List.of() : addressList);
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }

    public GenericResponse getUserAddressByAddressId(UUID userId,UUID addressId) throws ShopServiceApiException {
        IUsersResp userProfile = usersRepo.getUserProfile(userId);

        if (userProfile == null)
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "User not found.");

        IAddressResp address = addressesRepo.findAddressesEntitiesByAddressId((addressId));

        if (address == null)
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "User address not found.");

        GenericResponse response = new GenericResponse();
        response.setData(address);
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }

    @Transactional
    public GenericResponse addUserAddress(UUID userId, AddUserAddressReq addUserAddressReq) throws ShopServiceApiException {

        if(addUserAddressReq.getIsDefault())
            checkIsDefaultAddressExist(userId);

        UsersEntity usersEntity = new UsersEntity();
        usersEntity.setUserId(userId);

        AddressesEntity newAddress = getAddressEntityByRequest(userId, addUserAddressReq);

        AddressesEntity savedData = addressesRepo.save(newAddress);

        GenericResponse response = new GenericResponse();
        response.setData(CustomizeResponseUtil.ReturnKeyValueWhenComplete(savedData.getAddressId()));
        response.setStatus(ResultCode.CREATED);
        return response;

    }

    @Transactional
    public GenericResponse syncUserAddress(UUID userId, SyncUserAddressReq syncUserAddressReq) throws ShopServiceApiException {

        UUID addressIdUUID = UUID.fromString(syncUserAddressReq.getAddressId());

        if(syncUserAddressReq.getIsDefault())
            checkIsDefaultAddressExist(userId);

        AddressesEntity addressOpt = addressesRepo
                .findAddressesEntitiesByAddressId_AndUsersEntity_UserId(addressIdUUID,userId);

        if(addressOpt == null)
            throw new ShopForbiddenException(ResultCode.FORBIDDEN, "User can only update own address.");

        addressOpt.setDeliveryAddress(syncUserAddressReq.getDeliveryAddress());
        addressOpt.setPostcode(syncUserAddressReq.getPostcode());
        addressOpt.setProvince(syncUserAddressReq.getProvince());
        addressOpt.setDistrict(syncUserAddressReq.getDistrict());
        addressOpt.setSubDistrict(syncUserAddressReq.getSubDistrict());
        addressOpt.setAddressLabel(syncUserAddressReq.getAddressLabel());
        addressOpt.setDefault(syncUserAddressReq.getIsDefault());
        addressOpt.setRecipientFirstName(syncUserAddressReq.getRecipientFirstName());
        addressOpt.setRecipientLastName(syncUserAddressReq.getRecipientLastName());
        addressOpt.setRecipientPhone(syncUserAddressReq.getRecipientPhone());

        addressesRepo.save(addressOpt);

        GenericResponse response = new GenericResponse();
        response.setData(null);
        response.setStatus(ResultCode.NO_CONTENT);
        return response;
    }

    @Transactional
    public GenericResponse deleteAddress(UUID userId, UUID addressId) throws ShopServiceApiException {
        AddressesEntity addressOpt = addressesRepo
                .findAddressesEntitiesByAddressId_AndUsersEntity_UserId(addressId,userId);

        if(addressOpt == null)
            throw new ShopForbiddenException(ResultCode.FORBIDDEN, "User can only delete own address.");

        addressesRepo.delete(addressOpt);

        GenericResponse response = new GenericResponse();
        response.setData(null);
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }

    private void checkIsDefaultAddressExist(UUID userId) throws  ShopServiceApiException {
        List<IAddressResp> addressList = addressesRepo.findAddressesEntitiesByUsersEntity_UserId(userId);
        boolean isDefaultAddressExist = addressList.stream().anyMatch(IAddressResp::getIsDefault);

        if(isDefaultAddressExist)
            throw new ShopConflictException(ResultCode.CONFLICT, "Default address already exist.");
    }

    private AddressesEntity getAddressEntityByRequest(UUID userId, AddUserAddressReq addUserAddressReq){
        UsersEntity usersEntity = new UsersEntity();
        usersEntity.setUserId(userId);

        AddressesEntity newAddress = new AddressesEntity();
        newAddress.setUsersEntity(usersEntity);
        newAddress.setDeliveryAddress(addUserAddressReq.getDeliveryAddress());
        newAddress.setPostcode(addUserAddressReq.getPostcode());
        newAddress.setProvince(addUserAddressReq.getProvince());
        newAddress.setDistrict(addUserAddressReq.getDistrict());
        newAddress.setSubDistrict(addUserAddressReq.getSubDistrict());
        newAddress.setAddressLabel(addUserAddressReq.getAddressLabel());
        newAddress.setDefault(addUserAddressReq.getIsDefault());
        newAddress.setRecipientFirstName(addUserAddressReq.getRecipientFirstName());

        return newAddress;
    }
}

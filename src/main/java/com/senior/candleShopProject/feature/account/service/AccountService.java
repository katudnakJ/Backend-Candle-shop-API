package com.senior.candleShopProject.feature.account.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.UserCheckTemp;
import com.senior.candleShopProject.common.exception.*;
import com.senior.candleShopProject.common.utils.Constants;
import com.senior.candleShopProject.common.utils.CustomizeResponseUtil;
import com.senior.candleShopProject.datasource.domain.address.IAddressResp;
import com.senior.candleShopProject.datasource.domain.users.IUsersResp;
import com.senior.candleShopProject.datasource.entities.AddressesEntity;
import com.senior.candleShopProject.datasource.entities.UsersEntity;
import com.senior.candleShopProject.datasource.repo.AddressesRepo;
import com.senior.candleShopProject.datasource.repo.UsersRepo;
import com.senior.candleShopProject.feature.account.controller.dto.request.AddUserAddressReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AddressesRepo addressesRepo;
    private final UserCheckTemp userCheckTemp;

    public GenericResponse getUserAddresses(String userRole, UUID userId) throws ShopServiceApiException {

        userCheckTemp.checkExistsUser(userId);

        List<IAddressResp> addressList;
        if ( userRole.equalsIgnoreCase(Constants.ROLE_SELLER ))
            addressList = addressesRepo.findAddressesEntitiesByIsDefaultTrueAndUsersEntity_UserId(userId);
        else
            addressList = addressesRepo.findAddressesEntitiesByUsersEntity_UserId(userId);

        GenericResponse response = new GenericResponse();
        response.setData(addressList.isEmpty() ? List.of() : addressList);
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }

    public GenericResponse getUserAddressByAddressId(UUID userId,UUID addressId) throws ShopServiceApiException {

        userCheckTemp.checkExistsUser(userId);

        IAddressResp address = addressesRepo.findAddressesEntitiesByAddressId((addressId));

        if (address == null)
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "ที่อยู่นี้ไม่มีอยู่ในระบบ", "User address not found.");

        GenericResponse response = new GenericResponse();
        response.setData(address);
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }

    @Transactional
    public GenericResponse addUserAddress(UUID userId, AddUserAddressReq addUserAddressReq) throws ShopServiceApiException {

        userCheckTemp.checkExistsUser(userId);

        List<IAddressResp> existingAddressList = addressesRepo.findAddressesEntitiesByUsersEntity_UserId(userId);

        if (existingAddressList.size() >= 5)
            throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "คุณสามารถเพิ่มที่อยู่ได้ไม่เกิน 5 ที่อยู่เท่านั้น" , "User can only have up to 5 addresses.");

        Optional<IAddressResp> defaultAddress = existingAddressList.stream()
                .filter(IAddressResp::getIsDefault)
                .findFirst();

//        if default address is already exist and is not the same. Then set exist default address to false before update new default address.
        if (addUserAddressReq.getIsDefault() && defaultAddress.isPresent()) {
            unsetDefaultAddress(defaultAddress,userId);
        }


        AddressesEntity newAddress = getAddressEntityByRequest(userId, addUserAddressReq);

        AddressesEntity savedData = addressesRepo.save(newAddress);

        GenericResponse response = new GenericResponse();
        response.setData(CustomizeResponseUtil.ReturnKeyValueWhenComplete(savedData.getAddressId()));
        response.setStatus(ResultCode.CREATED);
        return response;

    }

    @Transactional
    public GenericResponse syncUserAddress(UUID userId, AddUserAddressReq syncUserAddressReq, UUID addressId) throws ShopServiceApiException {

        AddressesEntity addressOpt = addressesRepo.findAddressesEntitiesByAddressId_AndUsersEntity_UserId(addressId,userId);

        if(addressOpt == null)
            throw new ShopForbiddenException(ResultCode.FORBIDDEN, "คุณสามารถแก้ไขที่อยู่ของตัวเองเท่านั้น" ,"User can only update own address.");

        List<IAddressResp> existingAddressList = addressesRepo.findAddressesEntitiesByUsersEntity_UserId(userId);

        Optional<IAddressResp> defaultAddress = existingAddressList.stream()
                .filter(IAddressResp::getIsDefault)
                .findFirst();

            UUID existingDefaultAddressId = defaultAddress
                .map(IAddressResp::getAddressId)
                .map(UUID::fromString)
                .orElse(null);
//        if default address is already exist and is not the same. Then set exist default address to false before update new default address.
        if (syncUserAddressReq.getIsDefault()&& defaultAddress.isPresent() && !existingDefaultAddressId.equals(addressId)) {
            unsetDefaultAddress(defaultAddress,userId);
        }

        AddressesEntity newAddress = getAddressEntityByRequest(userId, syncUserAddressReq);
        newAddress.setAddressId(addressOpt.getAddressId());

        addressesRepo.save(newAddress);

        GenericResponse response = new GenericResponse();
        response.setData(null);
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }

    @Transactional
    public GenericResponse deleteAddress(UUID userId, UUID addressId) throws ShopServiceApiException {
        AddressesEntity addressOpt = addressesRepo
                .findAddressesEntitiesByAddressId_AndUsersEntity_UserId(addressId,userId);

        if(addressOpt == null)
            throw new ShopForbiddenException(ResultCode.FORBIDDEN, "คุณสามารถลบที่อยู่ของตัวเองเท่านั้น" ,"User can only delete own address.");

        if (addressOpt.isDefault())
            throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "ไม่สามารถลบที่อยู่เริ่มต้นได้" , "User can not delete default address.");

        addressesRepo.delete(addressOpt);

        GenericResponse response = new GenericResponse();
        response.setData(null);
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }

    private AddressesEntity getAddressEntityByRequest(UUID userId, AddUserAddressReq addUserAddressReq){
        UsersEntity usersEntity = new UsersEntity();
        usersEntity.setUserId(userId);

        AddressesEntity newAddress = new AddressesEntity();
        newAddress.setDeliveryAddress(addUserAddressReq.getDeliveryAddress());
        newAddress.setPostcode(addUserAddressReq.getPostcode());
        newAddress.setProvince(addUserAddressReq.getProvince());
        newAddress.setDistrict(addUserAddressReq.getDistrict());
        newAddress.setSubDistrict(addUserAddressReq.getSubDistrict());
        newAddress.setAddressLabel(addUserAddressReq.getAddressLabel());
        newAddress.setDefault(addUserAddressReq.getIsDefault());
        newAddress.setRecipientFirstName(addUserAddressReq.getRecipientFirstName());
        newAddress.setRecipientLastName(addUserAddressReq.getRecipientLastName());
        newAddress.setRecipientPhone(addUserAddressReq.getRecipientPhone());
        newAddress.setUsersEntity(usersEntity);

        return newAddress;
    }

    private void unsetDefaultAddress(Optional<IAddressResp> defaultAddress, UUID userId) {
        AddressesEntity existDefaultAddress = new AddressesEntity();

        UsersEntity usersEntity = new UsersEntity();
        usersEntity.setUserId(userId);

        IAddressResp iAddress = defaultAddress.get();
        existDefaultAddress.setAddressId(UUID.fromString(iAddress.getAddressId()));
        existDefaultAddress.setDeliveryAddress(iAddress.getDeliveryAddress());
        existDefaultAddress.setPostcode(iAddress.getPostcode());
        existDefaultAddress.setProvince(iAddress.getProvince());
        existDefaultAddress.setDistrict(iAddress.getDistrict());
        existDefaultAddress.setSubDistrict(iAddress.getSubDistrict());
        existDefaultAddress.setAddressLabel(iAddress.getAddressLabel());
        existDefaultAddress.setDefault(false);
        existDefaultAddress.setRecipientFirstName(iAddress.getRecipientFirstName());
        existDefaultAddress.setRecipientLastName(iAddress.getRecipientLastName());
        existDefaultAddress.setRecipientPhone(iAddress.getRecipientPhone());
        existDefaultAddress.setUsersEntity(usersEntity);
        addressesRepo.save(existDefaultAddress);
    }
}

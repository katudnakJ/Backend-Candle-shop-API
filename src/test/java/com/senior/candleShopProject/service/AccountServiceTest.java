package com.senior.candleShopProject.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.UserCheckTemp;
import com.senior.candleShopProject.common.exception.*;
import com.senior.candleShopProject.datasource.domain.address.IAddressResp;
import com.senior.candleShopProject.datasource.domain.users.IUsersResp;
import com.senior.candleShopProject.datasource.entities.AddressesEntity;
import com.senior.candleShopProject.datasource.repo.AddressesRepo;
import com.senior.candleShopProject.datasource.repo.UsersRepo;
import com.senior.candleShopProject.feature.account.controller.dto.request.AddUserAddressReq;
import com.senior.candleShopProject.feature.account.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private UsersRepo usersRepo;

    @Mock
    private AddressesRepo addressesRepo;

    @Mock
    private IUsersResp userProfile;

    @Mock
    private IAddressResp addressResp;

    @Mock
    private UserCheckTemp userCheckTemp;

    @InjectMocks
    private AccountService accountService;

    @Test
    void getUserAddress_success_whenUserAndAddressExist() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();

        when(usersRepo.getUserProfile(userId)).thenReturn(userProfile);
        when(addressesRepo.findAddressesEntitiesByUsersEntity_UserId(userId))
                .thenReturn(List.of(addressResp));

        GenericResponse response = accountService.getUserAddresses(userId);

        assertNotNull(response.getData());
        assertInstanceOf(List.class, response.getData());
        verify(usersRepo).getUserProfile(userId);
        verify(addressesRepo).findAddressesEntitiesByUsersEntity_UserId(userId);
    }

    @Test
    void getUserAddress_throw_whenUserNotFound() {
        UUID userId = UUID.randomUUID();

        when(usersRepo.getUserProfile(userId)).thenReturn(null);

        assertThrows(ShopDataNotFoundException.class,
                () -> accountService.getUserAddresses(userId));

        verify(usersRepo).getUserProfile(userId);
        verifyNoInteractions(addressesRepo);
    }

    @Test
    void addUserAddress_success_whenNotDefault() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();

        AddUserAddressReq req = mock(AddUserAddressReq.class);
        when(req.getIsDefault()).thenReturn(false);
        when(req.getDeliveryAddress()).thenReturn("addr");
        when(req.getPostcode()).thenReturn("10110");
        when(req.getProvince()).thenReturn("BKK");
        when(req.getDistrict()).thenReturn("District");
        when(req.getSubDistrict()).thenReturn("Sub");
        when(req.getAddressLabel()).thenReturn("Home");
        when(req.getRecipientFirstName()).thenReturn("John");

        IAddressResp mockAddress = mock(IAddressResp.class);
        when(mockAddress.getIsDefault()).thenReturn(true);

        when(addressesRepo.findAddressesEntitiesByUsersEntity_UserId(any(UUID.class)))
                .thenReturn(List.of(mockAddress));

        AddressesEntity addressesEntity = mock(AddressesEntity.class);
        when(addressesRepo.save(any(AddressesEntity.class))).thenReturn(addressesEntity);

        GenericResponse response = accountService.addUserAddress(userId, req);

        verify(addressesRepo).save(any(AddressesEntity.class));
    }

    @Test
    void syncUserAddress_success_whenAddressOwnedByUser() throws ShopServiceApiException {

        UUID userId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        AddUserAddressReq req = new AddUserAddressReq();
        req.setIsDefault(false);
        req.setDeliveryAddress("new addr");
        req.setPostcode("20000");
        req.setProvince("Province");
        req.setDistrict("District");
        req.setSubDistrict("SubDistrict");
        req.setAddressLabel("Office");
        req.setRecipientFirstName("Jane");
        req.setRecipientLastName("Doe");
        req.setRecipientPhone("0123456789");

        IAddressResp mockAddress = mock(IAddressResp.class);
        UUID defaultId = UUID.randomUUID();

        when(mockAddress.getIsDefault()).thenReturn(true);
        when(mockAddress.getAddressId()).thenReturn(defaultId.toString());

        when(addressesRepo.findAddressesEntitiesByUsersEntity_UserId(any(UUID.class)))
                .thenReturn(List.of(mockAddress));

        AddressesEntity existingInDb = new AddressesEntity();
        existingInDb.setAddressId(addressId);

        when(addressesRepo.findAddressesEntitiesByAddressId_AndUsersEntity_UserId(addressId, userId))
                .thenReturn(existingInDb);

        GenericResponse response = accountService.syncUserAddress(userId, req, addressId);


        ArgumentCaptor<AddressesEntity> captor =
                ArgumentCaptor.forClass(AddressesEntity.class);

        verify(addressesRepo).save(captor.capture());

        AddressesEntity savedEntity = captor.getValue();

        assertEquals("new addr", savedEntity.getDeliveryAddress());
        assertEquals("20000", savedEntity.getPostcode());
        assertEquals("Province", savedEntity.getProvince());
        assertEquals("District", savedEntity.getDistrict());
        assertEquals("SubDistrict", savedEntity.getSubDistrict());
        assertEquals("Office", savedEntity.getAddressLabel());

        assertEquals("Jane", savedEntity.getRecipientFirstName());
        assertEquals("Doe", savedEntity.getRecipientLastName());
        assertEquals("0123456789", savedEntity.getRecipientPhone());

        assertFalse(savedEntity.isDefault());
    }

    @Test
    void syncUserAddress_throwForbidden_whenAddressNotFound() {
        UUID userId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        AddUserAddressReq req = new AddUserAddressReq();
        req.setIsDefault(false);
        req.setDeliveryAddress("new addr");
        req.setPostcode("20000");
        req.setProvince("Province");
        req.setDistrict("District");
        req.setSubDistrict("SubDistrict");
        req.setAddressLabel("Office");
        req.setRecipientFirstName("Jane");
        req.setRecipientLastName("Doe");
        req.setRecipientPhone("0123456789");

        when(addressesRepo.findAddressesEntitiesByAddressId_AndUsersEntity_UserId(addressId, userId))
                .thenReturn(null);

        assertThrows(ShopForbiddenException.class,
                () -> accountService.syncUserAddress(userId, req, addressId));

        verify(addressesRepo).findAddressesEntitiesByAddressId_AndUsersEntity_UserId(addressId, userId);
        verify(addressesRepo, never()).save(any());
    }



    @Test
    void deleteAddress_success_whenAddressOwnedByUser() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        AddressesEntity entity = new AddressesEntity();
        when(addressesRepo.findAddressesEntitiesByAddressId_AndUsersEntity_UserId(addressId, userId))
                .thenReturn(entity);

        GenericResponse response = accountService.deleteAddress(userId, addressId);

        verify(addressesRepo).delete(entity);
    }

    @Test
    void deleteAddress_throwForbidden_whenAddressNotFound() {
        UUID userId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        when(addressesRepo.findAddressesEntitiesByAddressId_AndUsersEntity_UserId(addressId, userId))
                .thenReturn(null);

        assertThrows(ShopForbiddenException.class,
                () -> accountService.deleteAddress(userId, addressId));

        verify(addressesRepo, never()).delete(any());
    }
}

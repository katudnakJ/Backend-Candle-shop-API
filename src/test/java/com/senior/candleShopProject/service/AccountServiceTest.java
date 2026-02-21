package com.senior.candleShopProject.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.*;
import com.senior.candleShopProject.datasource.domain.IAddressResp;
import com.senior.candleShopProject.datasource.domain.IUsersResp;
import com.senior.candleShopProject.datasource.entities.AddressesEntity;
import com.senior.candleShopProject.datasource.repo.AddressesRepo;
import com.senior.candleShopProject.datasource.repo.UsersRepo;
import com.senior.candleShopProject.feature.account.controller.dto.request.AddUserAddressReq;
import com.senior.candleShopProject.feature.account.controller.dto.request.SyncUserAddressReq;
import com.senior.candleShopProject.feature.account.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @InjectMocks
    private AccountService accountService;

    @Test
    void getUserAddress_success_whenUserAndAddressExist() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();

        when(usersRepo.getUserProfile(userId)).thenReturn(userProfile);
        when(addressesRepo.findAddressesEntitiesByUsersEntity_UserId(userId))
                .thenReturn(List.of(addressResp));

        GenericResponse response = accountService.getUserAddresses(userId);

        assertEquals(ResultCode.SUCCESS, response.getStatus());
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

        AddressesEntity addressesEntity = mock(AddressesEntity.class);
        when(addressesRepo.save(any(AddressesEntity.class))).thenReturn(addressesEntity);

        GenericResponse response = accountService.addUserAddress(userId, req);

        assertEquals(ResultCode.CREATED, response.getStatus());
        verify(addressesRepo).save(any(AddressesEntity.class));
    }

    @Test
    void addUserAddress_throw_whenDefaultAlreadyExist() {
        UUID userId = UUID.randomUUID();

        AddUserAddressReq req = mock(AddUserAddressReq.class);
        when(req.getIsDefault()).thenReturn(true);

        when(addressesRepo.findAddressesEntitiesByUsersEntity_UserId(userId))
                .thenReturn(List.of(addressResp));
        when(addressResp.getIsDefault()).thenReturn(true);

        assertThrows(ShopConflictException.class,
                () -> accountService.addUserAddress(userId, req));

        verify(addressesRepo).findAddressesEntitiesByUsersEntity_UserId(userId);
        verify(addressesRepo, never()).save(any());
    }

    @Test
    void syncUserAddress_success_whenAddressOwnedByUser() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        SyncUserAddressReq req = mock(SyncUserAddressReq.class);
        when(req.getAddressId()).thenReturn(addressId.toString());
        when(req.getIsDefault()).thenReturn(false);
        when(req.getDeliveryAddress()).thenReturn("new addr");
        when(req.getPostcode()).thenReturn("20000");
        when(req.getProvince()).thenReturn("Province");
        when(req.getDistrict()).thenReturn("District");
        when(req.getSubDistrict()).thenReturn("SubDistrict");
        when(req.getAddressLabel()).thenReturn("Office");
        when(req.getRecipientFirstName()).thenReturn("Jane");
        when(req.getRecipientLastName()).thenReturn("Doe");
        when(req.getRecipientPhone()).thenReturn("0123456789");

        AddressesEntity entity = new AddressesEntity();
        when(addressesRepo.findAddressesEntitiesByAddressId_AndUsersEntity_UserId(addressId, userId))
                .thenReturn(entity);

        GenericResponse response = accountService.syncUserAddress(userId, req);

        assertEquals(ResultCode.SUCCESS, response.getStatus());
        verify(addressesRepo).save(entity);
        assertEquals("new addr", entity.getDeliveryAddress());
        assertEquals("20000", entity.getPostcode());
        assertEquals("Province", entity.getProvince());
        assertEquals("District", entity.getDistrict());
        assertEquals("SubDistrict", entity.getSubDistrict());
        assertEquals("Office", entity.getAddressLabel());
        assertFalse(entity.isDefault());
        assertEquals("Jane", entity.getRecipientFirstName());
        assertEquals("Doe", entity.getRecipientLastName());
        assertEquals("0123456789", entity.getRecipientPhone());
    }

    @Test
    void syncUserAddress_throwForbidden_whenAddressNotFound() {
        UUID userId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        SyncUserAddressReq req = mock(SyncUserAddressReq.class);
        when(req.getAddressId()).thenReturn(addressId.toString());
        when(req.getIsDefault()).thenReturn(false);

        when(addressesRepo.findAddressesEntitiesByAddressId_AndUsersEntity_UserId(addressId, userId))
                .thenReturn(null);

        assertThrows(ShopForbiddenException.class,
                () -> accountService.syncUserAddress(userId, req));

        verify(addressesRepo).findAddressesEntitiesByAddressId_AndUsersEntity_UserId(addressId, userId);
        verify(addressesRepo, never()).save(any());
    }

    @Test
    void syncUserAddress_throwInvalid_whenSetDefaultAndDefaultAlreadyExist() {
        UUID userId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        SyncUserAddressReq req = mock(SyncUserAddressReq.class);
        when(req.getAddressId()).thenReturn(addressId.toString());
        when(req.getIsDefault()).thenReturn(true);

        when(addressesRepo.findAddressesEntitiesByUsersEntity_UserId(userId))
                .thenReturn(List.of(addressResp));
        when(addressResp.getIsDefault()).thenReturn(true);

        assertThrows(ShopConflictException.class,
                () -> accountService.syncUserAddress(userId, req));

        verify(addressesRepo).findAddressesEntitiesByUsersEntity_UserId(userId);
        verify(addressesRepo, never())
                .findAddressesEntitiesByAddressId_AndUsersEntity_UserId(any(), any());
    }

    @Test
    void deleteAddress_success_whenAddressOwnedByUser() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        AddressesEntity entity = new AddressesEntity();
        when(addressesRepo.findAddressesEntitiesByAddressId_AndUsersEntity_UserId(addressId, userId))
                .thenReturn(entity);

        GenericResponse response = accountService.deleteAddress(userId, addressId);

        assertEquals(ResultCode.SUCCESS, response.getStatus());
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

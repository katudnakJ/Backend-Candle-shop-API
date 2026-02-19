package com.senior.candleShopProject.datasource.repo;

import com.senior.candleShopProject.datasource.domain.IAddressResp;
import com.senior.candleShopProject.datasource.entities.AddressesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressesRepo extends JpaRepository<AddressesEntity, UUID> {

    List<IAddressResp> findAddressesEntitiesByUsersEntity_UserId(UUID userId);
    AddressesEntity findAddressesEntitiesByAddressId_AndUsersEntity_UserId(UUID addressId, UUID userId);

    IAddressResp findAddressesEntitiesByAddressId(UUID addressId);
}

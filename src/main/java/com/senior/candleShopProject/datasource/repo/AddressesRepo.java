package com.senior.candleShopProject.datasource.repo;

import com.senior.candleShopProject.datasource.domain.IAddressResp;
import com.senior.candleShopProject.datasource.entities.AddressesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AddressesRepo extends JpaRepository<AddressesEntity, UUID> {

    @Query(value = """
            select address_id as addressId,
                user_id as userId,
                delivery_address as deliveryAddress,
                postcode as postcode,
                province as province,
                district as district,
                sub_district as subDistrct,
                address_label as addressLabel,
                is_default as isDefault,
                recipient_first_name as recipientFirstName,
                recipient_last_name as recipientLastName,
                recipient_phone as recipientPhone
            from addresses
            where user_id = :userId;
""", nativeQuery = true)
    List<IAddressResp> findAddressesByUsersId(UUID userId);
}

package com.senior.candleShopProject.datasource.repo;

import com.senior.candleShopProject.datasource.entities.OrderShippingAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderShippingAddressRepo extends JpaRepository<OrderShippingAddressEntity, UUID> {
}

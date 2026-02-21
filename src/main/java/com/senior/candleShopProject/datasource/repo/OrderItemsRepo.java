package com.senior.candleShopProject.datasource.repo;

import com.senior.candleShopProject.datasource.entities.OrderItemsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderItemsRepo extends JpaRepository<OrderItemsEntity, UUID> {
}

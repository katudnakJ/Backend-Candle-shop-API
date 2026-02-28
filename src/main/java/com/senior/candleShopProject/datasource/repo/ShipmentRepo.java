package com.senior.candleShopProject.datasource.repo;

import com.senior.candleShopProject.datasource.entities.ShipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipmentRepo extends JpaRepository<ShipmentEntity, Integer> {
}

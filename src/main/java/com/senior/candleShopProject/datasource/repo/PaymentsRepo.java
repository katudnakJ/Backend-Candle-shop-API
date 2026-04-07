package com.senior.candleShopProject.datasource.repo;

import com.senior.candleShopProject.datasource.entities.PaymentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentsRepo extends JpaRepository<PaymentsEntity, UUID> {
    @Query(value = "SELECT NEXTVAL('receipt_no_seq')", nativeQuery = true)
    Long getNextReceiptNo();

    boolean existsByOrdersEntity_OrderId(UUID orderId);

    PaymentsEntity findPaymentsEntitiesByOrdersEntity_OrderId(UUID orderId);

}

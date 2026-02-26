package com.senior.candleShopProject.datasource.repo;

import com.senior.candleShopProject.datasource.entities.CustomersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomersRepo extends JpaRepository<CustomersEntity, UUID> {

    Optional<CustomersEntity> findCustomersEntitiesByUsersEntity_UserId(UUID customerId);

    CustomersEntity getCustomersEntityByUsersEntity_UserId(UUID usersEntityUserId);

    boolean existsCustomersEntitiesByUsersEntity_UserId(UUID userId);
}

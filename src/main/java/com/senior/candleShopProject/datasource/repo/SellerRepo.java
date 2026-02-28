package com.senior.candleShopProject.datasource.repo;

import com.senior.candleShopProject.datasource.domain.users.ISellerResp;
import com.senior.candleShopProject.datasource.entities.SellerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SellerRepo extends JpaRepository<SellerEntity, UUID> {

    @Query(value = """
    select seller_id AS sellerId,
        qr_payment_img_path AS qrPaymentImgPath
    from seller
    where user_id = :userId
""",nativeQuery = true)
    ISellerResp getSellerByUserId(@Param(value = "userId") UUID userId);

    SellerEntity getSellerEntitiesByUsersEntity_UserId(UUID usersEntityUserId);

}

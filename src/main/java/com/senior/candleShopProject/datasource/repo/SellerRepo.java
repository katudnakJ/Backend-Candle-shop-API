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
        select s.seller_id AS sellerId,
            s.qr_payment_img_path AS qrPaymentImgPath
        from seller s
        where s.is_owner = true;
""", nativeQuery = true)
    ISellerResp getQrPaymentImagePath(@Param(value = "userId") UUID userId);

    SellerEntity getSellerEntitiesByUsersEntity_UserId(UUID usersEntityUserId);

    @Query(value = """
        select s.is_owner
        from seller s
        where s.user_id = :userId
""", nativeQuery = true)
    Boolean getIsOwnerByUserId(@Param(value = "userId") UUID userId);

}

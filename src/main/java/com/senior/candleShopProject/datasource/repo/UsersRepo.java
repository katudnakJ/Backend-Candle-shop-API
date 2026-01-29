package com.senior.candleShopProject.datasource.repo;

import com.senior.candleShopProject.datasource.domain.IUsersResp;
import com.senior.candleShopProject.datasource.domain.UserCreate;
import com.senior.candleShopProject.datasource.entities.UsersEntity;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UsersRepo extends JpaRepository <UsersEntity, UUID> {
    @Query(value = """
       select user_id AS userId,
              line_display_name AS lineDisplayName,
              is_seller AS isSeller,
              user_role AS userRole
              from users
       where user_id = :userId;
        """, nativeQuery = true)
    IUsersResp getUserProfile(@Param("userId") UUID userId);

    @Query(value = """
       select user_id AS userId,
              line_display_name AS lineDisplayName,
              is_seller AS isSeller,
              user_role AS userRole
              from users
       where line_id = :lineId;
        """, nativeQuery = true)
    IUsersResp getUserProfileByLineId(@Param("lineId") String lineId);

}

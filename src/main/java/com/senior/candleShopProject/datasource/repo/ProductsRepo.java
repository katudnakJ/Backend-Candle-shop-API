package com.senior.candleShopProject.datasource.repo;

import com.senior.candleShopProject.datasource.entities.ProductsEntity;
import com.senior.candleShopProject.feature.product.controller.dto.domain.IProductResp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductsRepo extends JpaRepository<ProductsEntity, UUID> {
    @Query(value = """
        select product_id AS product_id,
            product_name AS productName,
            price AS price,
            weight AS weight,
            description AS description,
            slug AS slug,
            is_active AS isActive,
            is_featured AS isFeature,
            product_created_date AS productCreateDate,
            product_updated_date AS productUpdatedDate,
            total_selled AS totalSelled
        from products
        where product_id = :productId;
        """, nativeQuery = true)
    IProductResp getProductById(@Param("productId") UUID productId);
}

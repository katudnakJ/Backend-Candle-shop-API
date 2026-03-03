package com.senior.candleShopProject.datasource.repo;

import com.senior.candleShopProject.datasource.entities.ProductImagesEntity;
import com.senior.candleShopProject.datasource.domain.products.IProductImagesResp;
import com.senior.candleShopProject.feature.product.controller.dto.response.ProductImagesResp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductImagesRepo extends JpaRepository<ProductImagesEntity, UUID> {

    @Query("SELECT new com.senior.candleShopProject.feature.product.controller.dto.response.ProductImagesResp" +
            "(i.productImgId AS productImgId, " +
            "i.productImgPath AS productImgPath, " +
            "i.isPrimary AS isPrimary) " +
            "FROM ProductImagesEntity i WHERE i.productsEntity.productId = :productId")
    List<ProductImagesResp> getProductImagesByProductId(@Param("productId") UUID productId);
}

package com.senior.candleShopProject.datasource.repo;

import com.senior.candleShopProject.datasource.entities.ProductImagesEntity;
import com.senior.candleShopProject.datasource.domain.products.IProductImagesResp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductImagesRepo extends JpaRepository<ProductImagesEntity, UUID> {
    @Query(value = """
        select product_img_id AS productImgId,
                product_img_path AS productImgPath,
                is_primary AS isPrimary
        from product_images
        where product_id = :productId;
        """,nativeQuery = true)
    List<IProductImagesResp> getProductImagesByProductId(@Param("productId") UUID productId);
}

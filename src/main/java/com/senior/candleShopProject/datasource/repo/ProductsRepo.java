package com.senior.candleShopProject.datasource.repo;

import com.senior.candleShopProject.datasource.domain.IProductHomeListItemResp;
import com.senior.candleShopProject.datasource.entities.ProductsEntity;
import com.senior.candleShopProject.datasource.domain.IProductResp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Query(value = """
    Select
      p.product_id as productId,
      p.product_name as productName,
      p.price as price,
      p.is_active as isActive,
      p.product_created_date,
      p.total_selled as totalSelled,
      pi.product_img_slug as productImgSlug
    from products p
    left join product_images pi
    on p.product_id = pi.product_id
    where is_primary = true
    and is_featured = :isFeatured
    order by p.product_id
    limit 20 offset 0;
    """,nativeQuery = true)
    List<IProductHomeListItemResp> getProductHomeListItemResp(@Param("isFeatured") Boolean isFeatured);

    @Query(value = """
        Select count(*)
        from products p left join product_images pi
        on p.product_id = pi.product_id
        where is_primary = true
        and is_featured = :isFeatured
        limit 20 offset 0;
    """,nativeQuery = true)
    Integer getCountProductHomeListItemResp(@Param("isFeatured") Boolean isFeatured);
}

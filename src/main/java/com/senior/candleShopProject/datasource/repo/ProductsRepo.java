package com.senior.candleShopProject.datasource.repo;

import com.senior.candleShopProject.datasource.domain.products.IProductHomeListItemResp;
import com.senior.candleShopProject.datasource.domain.products.ProductByOrderIdResp;
import com.senior.candleShopProject.datasource.entities.ProductsEntity;
import com.senior.candleShopProject.datasource.domain.products.IProductResp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductsRepo extends JpaRepository<ProductsEntity, UUID> {
    @Query(value = """
        select product_id AS productId,
            product_name AS productName,
            price AS price,
            weight AS weight,
            description AS description,
            slug AS slug,
            is_active AS isActive,
            is_featured AS isFeature,
            product_created_date AS productCreateDate,
            product_updated_date AS productUpdatedDate,
            total_sold AS totalSold
        from products
        where product_id = :productId;
        """, nativeQuery = true)
    IProductResp getProductById(@Param("productId") UUID productId);

    @Query(value = """
          Select
              p.product_id as productId,
              p.product_name as productName,
              p.price as price,
              p.product_created_date as productCreatedDate,
              p.total_sold as totalSold,
              p.slug as productSlug,
              pi.product_img_path as productImgPath,
              p.is_active as isActive
            from products p
            left join product_images pi
            on p.product_id = pi.product_id
            and pi.is_primary = true
            where p.is_featured = :isFeatured
            and p.is_active = true
            ORDER BY  p.total_sold DESC
            LIMIT 5;
    """,nativeQuery = true)
    List<IProductHomeListItemResp> getProductHomeListItemByFeature(@Param("isFeatured") Boolean isFeatured);

    @Query(value = """
       Select
              p.product_id as productId,
              p.product_name as productName,
              p.price as price,
              p.product_created_date as productCreatedDate,
              p.total_sold as totalSold,
              p.slug as productSlug,
              pi.product_img_path as productImgPath,
              p.is_active as isActive
            from products p
            left join product_images pi
            on p.product_id = pi.product_id
            and pi.is_primary = true
            where ( :isSeller = true or p.is_active = true)
            ORDER BY  p.product_created_date ASC
            LIMIT :limit OFFSET :offset;
""", nativeQuery = true)
    List<IProductHomeListItemResp> getAllProductHomeList(@Param("isSeller") boolean isSeller , @Param("limit") int limit, @Param("offset") int offset);

    ProductsEntity findProductsEntityByProductId(UUID productId);

    @Query(value = """
        select p.product_id as productId,
            p.product_name as productName,
            p.price as price,
            p.product_created_date,
            p.total_sold as totalSold,
            p.slug as productSlug,
            pi.product_img_path as productImgPath,
            p.is_active as isActive
        from products p
        left join product_images pi
        on p.product_id = pi.product_id
        and pi.is_primary = true
        where LOWER(p.product_name) like LOWER(CONCAT('%', :query, '%'))
        and ( :isSeller = true or p.is_active = true)
        order by p.product_created_date ASC
        LIMIT :limit OFFSET :offset
""", nativeQuery = true)
    List<IProductHomeListItemResp> searchProductsByName(
           @Param("isSeller") boolean isSeller, @Param("query") String query, @Param("limit") int limit, @Param("offset") int offset
    );

    Long countByProductNameContainingIgnoreCase(String query);

    @Query(value = """
        select p.product_id AS productId,
               p.product_name AS productName,
               p.price AS price,
               p.weight AS weight,
               p.description AS description,
               p.slug AS slug,
               p.is_active AS isActive,
               p.is_featured AS isFeature,
               p.product_created_date AS productCreateDate,
               p.product_updated_date AS productUpdatedDate,
               p.total_sold AS totalSold,
               oi.quantity as itemSoldQuantity
        from products p
        join order_items oi on p.product_id = oi.product_id
        join orders o on oi.order_id = o.order_id
        where oi.order_id = :orderId
        and ( :isSeller = true or p.is_active = true);
""",nativeQuery = true)
    List<ProductByOrderIdResp> getProductsByOrderId(
            @Param("isSeller") boolean isSeller,
            @Param("orderId") UUID orderId
    );

    Long countProductsEntityByIsActive(boolean isActive);

    Long countByProductNameContainingIgnoreCaseAndIsActiveTrue(String query);
}

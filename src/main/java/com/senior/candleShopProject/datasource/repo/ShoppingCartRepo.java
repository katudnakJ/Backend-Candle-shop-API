package com.senior.candleShopProject.datasource.repo;

import com.senior.candleShopProject.datasource.domain.IAllItemsShoppingCartResp;
import com.senior.candleShopProject.datasource.entities.ShoppingCartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShoppingCartRepo extends JpaRepository<ShoppingCartEntity, UUID> {
    @Query(value = """
            SELECT sc.shopping_cart_id AS shoppingCartId,
                sci.shopping_cart_item_id AS shoppingCartItemId,
                p.product_id AS productId,
                sci.quantity AS quantity,
                p.product_name AS productName,
                p.price AS price,
                p.weight AS weight,
                p.description AS description,
                p.slug AS productSlug,
                pi.product_img_path AS productImgPath
            FROM users u
            JOIN  customers c ON u.user_id = c.user_id
            JOIN shopping_cart sc ON c.customer_id = sc.customer_id
            JOIN shopping_cart_items sci ON sc.shopping_cart_id = sci.shopping_cart_id
            JOIN products p
            ON sci.product_id = p.product_id
            LEFT JOIN product_images pi
            ON p.product_id = pi.product_id AND pi.is_primary = true
            WHERE u.user_id = :userId;
            """, nativeQuery = true)
    List<IAllItemsShoppingCartResp> getAllItemsFromShoppingCartByUserId(@Param("userId") UUID userId);

    @Query(value = """
    select shopping_cart_id AS shoppingCartId
    from shopping_cart
    where customer_id = (
      select customer_id
      from customers
      where user_id = :userId
    );
""", nativeQuery = true)
    UUID getShoppingCartIdByUserId(@Param("userId") UUID userId);
}

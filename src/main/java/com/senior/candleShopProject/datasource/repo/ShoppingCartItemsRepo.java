package com.senior.candleShopProject.datasource.repo;

import com.senior.candleShopProject.datasource.domain.ICartItemsForOrderItemsResp;
import com.senior.candleShopProject.datasource.entities.ShoppingCartItemsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShoppingCartItemsRepo extends JpaRepository<ShoppingCartItemsEntity, UUID> {

    Optional<ShoppingCartItemsEntity> findShoppingCartItemsEntitiesByShoppingCartEntity_ShoppingCartIdAndProductsEntity_ProductId(UUID shoppingCartId, UUID productId);

    Boolean existsByShoppingCartItemId(UUID shoppingCartItemId);
    Boolean existsByShoppingCartEntity_ShoppingCartId(UUID shoppingCartId);

    @Query(value = """
        select sci.quantity as quantity,
            p.product_id as productId,
            p.product_name as productName,
            p.price as pricePerUnit
        from shopping_cart_items sci
        join products p
        on sci.product_id = p.product_id
        where sci.shopping_cart_id = :shoppingCartId
        and sci.shopping_cart_item_id in :cartItemIdList;
""",nativeQuery = true)
    List<ICartItemsForOrderItemsResp> getCartItemsForOrderItemsByCartIdAndCartItemIdList(UUID shoppingCartId, List<UUID> cartItemIdList);
}

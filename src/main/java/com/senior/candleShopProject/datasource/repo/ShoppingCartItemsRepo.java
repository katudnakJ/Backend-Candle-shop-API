package com.senior.candleShopProject.datasource.repo;

import com.senior.candleShopProject.datasource.entities.ShoppingCartItemsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShoppingCartItemsRepo extends JpaRepository<ShoppingCartItemsEntity, UUID> {

    Optional<ShoppingCartItemsEntity> findShoppingCartItemsEntitiesByShoppingCartEntity_ShoppingCartIdAndProductsEntity_ProductId(UUID shoppingCartId, UUID productId);

    Boolean existsByShoppingCartItemId(UUID shoppingCartItemId);
}

package com.senior.candleShopProject.datasource.entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "shopping_cart_items", uniqueConstraints = @UniqueConstraint(columnNames = {"shopping_cart_id", "product_id"}))
public class ShoppingCartItemsEntity {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "shopping_cart_item_id")
    private UUID shoppingCartItemId;

    @Column(name = "quantity")
    private Integer quantity;

//    Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopping_cart_id", nullable = false)
    private ShoppingCartEntity shoppingCartEntity;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductsEntity productsEntity;
}

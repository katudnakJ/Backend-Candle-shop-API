package com.senior.candleShopProject.datasource.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "shopping_cart")
public class ShoppingCartEntity {
    @Id
    @GeneratedValue
    @UuidGenerator( style = UuidGenerator.Style.TIME)
    @Column(name = "shopping_cart_id")
    private UUID shoppingCartId;

//    Relationships
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomersEntity customersEntity;

    @OneToMany(mappedBy = "shoppingCartEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShoppingCartItemsEntity> shoppingCartItemsEntities= new ArrayList<>();
}

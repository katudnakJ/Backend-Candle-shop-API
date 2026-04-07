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
@Table(name="customers")
public class CustomersEntity {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "customer_id")
    private UUID customerId;

//    Relationships
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UsersEntity usersEntity;

    @OneToMany(mappedBy = "customersEntity",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdersEntity> ordersEntities = new ArrayList<>();

    @OneToOne(mappedBy = "customersEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private ShoppingCartEntity shoppingCartEntity;
}

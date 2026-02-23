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
@Table(name = "users")
public class UsersEntity {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "line_id")
    private String lineId;

    @Column(name="is_seller")
    private boolean isSeller;

    @Column(name="user_role")
    private String userRole;

//    Relationships
    @OneToOne(mappedBy = "usersEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private CustomersEntity customersEntity;

    @OneToOne(mappedBy = "usersEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private SellerEntity sellerEntity;

    @OneToMany(mappedBy = "usersEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AddressesEntity> addressesEntities = new ArrayList<>();
}

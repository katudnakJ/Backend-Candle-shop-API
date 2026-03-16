package com.senior.candleShopProject.datasource.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name="products")
public class ProductsEntity {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "weight")
    private double weight;

    @Column(name = "description")
    private String description;

    @Column(name = "slug")
    private String slug;

    @Column(name="is_active")
    private boolean isActive;

    @Column(name = "is_featured")
    private boolean isFeatured;

    @Column(name = "product_created_date")
    private Instant productCreatedDate;

    @Column(name = "product_updated_date")
    private Instant productUpdatedDate;

    @Column(name = "total_selled")
    private Integer totalSold;

//    Relationships
    @OneToMany(mappedBy = "productsEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImagesEntity> productImagesEntities = new ArrayList<>();

    @OneToOne(mappedBy = "productsEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private ShoppingCartItemsEntity shoppingCartItemsEntity;

    @OneToMany(mappedBy = "productsEntity")
    private List<OrderItemsEntity> orderItemsEntities = new ArrayList<>();
}

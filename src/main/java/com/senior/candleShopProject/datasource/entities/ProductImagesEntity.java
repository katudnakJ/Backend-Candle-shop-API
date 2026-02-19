package com.senior.candleShopProject.datasource.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "product_images")
public class ProductImagesEntity {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "product_img_id")
    private UUID productImgId;

    @Column(name = "product_img_path")
    private String productImgPath;

    @Column(name = "is_primary")
    private boolean isPrimary;

//    Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductsEntity productsEntity;
}

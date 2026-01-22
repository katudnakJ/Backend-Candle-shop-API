package com.senior.candleShopProject.datasource.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "product_images")
public class ProductImagesEntity {
    @Id
    @Column(name = "product_img_id")
    private UUID productImgId;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "img_slug")
    private String imgSlug;

    @Column(name = "is_primary")
    private Boolean isPrimary;
}

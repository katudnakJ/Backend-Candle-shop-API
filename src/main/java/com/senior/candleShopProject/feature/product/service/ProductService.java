package com.senior.candleShopProject.feature.product.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopDataNotFoundException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.datasource.domain.IProductHomeListItemResp;
import com.senior.candleShopProject.datasource.repo.ProductImagesRepo;
import com.senior.candleShopProject.datasource.repo.ProductsRepo;
import com.senior.candleShopProject.datasource.domain.IProductImagesResp;
import com.senior.candleShopProject.datasource.domain.IProductResp;
import com.senior.candleShopProject.datasource.domain.ProductHomeListItemResp;
import com.senior.candleShopProject.feature.product.controller.dto.response.ProductImagesResp;
import com.senior.candleShopProject.feature.product.controller.dto.response.ProductDetailResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    @Value("${supabase.storage.public-image-base-url}")
    private String publicImageBaseUrl;

    private final ProductsRepo productsRepo;
    private final ProductImagesRepo productImagesRepo;

    public GenericResponse getProductsById(UUID productId) throws ShopServiceApiException {

        IProductResp product = productsRepo.getProductById(productId);

        if (product == null)
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "Product not found.");

        List<IProductImagesResp> images = productImagesRepo.getProductImagesByProductId(productId);

        List<ProductImagesResp> productImages = images.stream().map(image -> {
            ProductImagesResp resp = new ProductImagesResp();
            resp.setProductImgId(image.getProductImgId());
            resp.setProductImgPath(publicImageBaseUrl+image.getProductImgPath());
            resp.setIsPrimary(image.getIsPrimary());
            return resp;
        }).toList();

        ProductDetailResp productDetailResp = new ProductDetailResp();
        productDetailResp.setProduct(product);
        productDetailResp.setProductImages(productImages);

        GenericResponse response = new GenericResponse();
        response.setData(productDetailResp);
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }

    public GenericResponse getProductHomeListItem() throws ShopServiceApiException {

//      featured products
        List<IProductHomeListItemResp> featuredProducts = productsRepo.getProductHomeListItemResp(true);

//      non-featured products
        List<IProductHomeListItemResp> nonFeaturedProducts = productsRepo.getProductHomeListItemResp(false);
        Integer nonFeaturedProductsCount = productsRepo.getCountProductHomeListItemResp(false);

        if (featuredProducts.isEmpty() && nonFeaturedProducts.isEmpty())
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "All Products list are empty.");

        ProductHomeListItemResp productHomeListItemResp = new ProductHomeListItemResp();
        productHomeListItemResp.setFeaturedProduct(addPrefixProductImgPath(featuredProducts));

        productHomeListItemResp.setNonFeaturedProduct(addPrefixProductImgPath(nonFeaturedProducts));
        productHomeListItemResp.setNonFeaturedTotal(nonFeaturedProductsCount);

        GenericResponse response = new GenericResponse();
        response.setData(productHomeListItemResp);
        response.setStatus(ResultCode.SUCCESS);
        return response;

    }

    private List<IProductHomeListItemResp> addPrefixProductImgPath (List<IProductHomeListItemResp> productHomeListItemResp) {
       return(
              productHomeListItemResp.stream().map(item -> new IProductHomeListItemResp(
                        item.getProductId(),
                        item.getProductName(),
                        item.getPrice(),
                        item.getIsActive(),
                        item.getProductCreatedDate(),
                        item.getTotalSelled(),
                        publicImageBaseUrl + item.getProductImgPath()
              )).toList());

    }
}

package com.senior.candleShopProject.feature.product.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.SupabaseService.SupabaseStorageService;
import com.senior.candleShopProject.common.UserCheckTemp;
import com.senior.candleShopProject.common.exception.*;
import com.senior.candleShopProject.common.utils.Constants;
import com.senior.candleShopProject.common.utils.CustomizeResponseUtil;
import com.senior.candleShopProject.common.utils.ImageValidationUtils;
import com.senior.candleShopProject.datasource.domain.products.IProductHomeListItemResp;
import com.senior.candleShopProject.datasource.entities.ProductImagesEntity;
import com.senior.candleShopProject.datasource.entities.ProductsEntity;
import com.senior.candleShopProject.datasource.repo.ProductImagesRepo;
import com.senior.candleShopProject.datasource.repo.ProductsRepo;
import com.senior.candleShopProject.datasource.domain.products.IProductResp;
import com.senior.candleShopProject.datasource.domain.products.ProductHomeListItemResp;
import com.senior.candleShopProject.feature.product.controller.dto.request.CreateNewProductReq;
import com.senior.candleShopProject.feature.product.controller.dto.response.ProductImagesResp;
import com.senior.candleShopProject.feature.product.controller.dto.response.ProductDetailResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.senior.candleShopProject.common.utils.ProcessImageUtil.processImageData;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final UserCheckTemp userCheckTemp;
    private final SupabaseStorageService supabaseStorageService;

    @Value("${supabase.storage.public-image-base-url}")
    private String publicImageBaseUrl;

    private final ProductsRepo productsRepo;
    private final ProductImagesRepo productImagesRepo;

    public GenericResponse getProductsById(UUID productId) throws ShopServiceApiException {

        IProductResp product = productsRepo.getProductById(productId);

        if (product == null)
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "Product not found.");

        List<ProductImagesResp> images = productImagesRepo.getProductImagesByProductId(productId);

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
        List<IProductHomeListItemResp> featureFromRepo = productsRepo.getProductHomeListItemResp(true);
        List<IProductHomeListItemResp> featuredProducts = addPrefixProductImgPath(featureFromRepo);

//      non-featured products
        List<IProductHomeListItemResp> nonFeaturedFromRepo = productsRepo.getProductHomeListItemResp(false);
        List<IProductHomeListItemResp> nonFeaturedProducts = addPrefixProductImgPath(nonFeaturedFromRepo);

        if (featureFromRepo.isEmpty() && nonFeaturedFromRepo.isEmpty())
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "All Products list are empty.");

        ProductHomeListItemResp productHomeListItemResp = new ProductHomeListItemResp();
        productHomeListItemResp.setFeaturedProduct((featuredProducts));

        productHomeListItemResp.setNonFeaturedProduct(addPrefixProductImgPath(nonFeaturedProducts));
        productHomeListItemResp.setNonFeaturedTotal(nonFeaturedProducts.size());

        GenericResponse response = new GenericResponse();
        response.setData(productHomeListItemResp);
        response.setStatus(ResultCode.SUCCESS);
        return response;

    }

    @Transactional
    public GenericResponse createNewProduct(
            UUID userId,CreateNewProductReq createNewProductReq, List<MultipartFile> productImagesReq, int primaryIndex
    ) throws ShopServiceApiException, IOException {

        if (createNewProductReq == null
                || productImagesReq.isEmpty()
                || primaryIndex < 0
                || productImagesReq.size() <= primaryIndex
        )throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "Request body is missing or invalid primary index.");

        UUID sellerId = userCheckTemp.getSellerIdByUserId(userId);

        if ( sellerId == null)
            throw new ShopUnAuthorizedException(ResultCode.UNAUTHORIZED, "You don't have permission to create new product.");

        ImageValidationUtils.validateImages(productImagesReq);

        ProductsEntity productEntity = new ProductsEntity();
        Instant timeNow = Instant.now();
        productEntity.setProductName(createNewProductReq.getProductName());
        productEntity.setPrice(createNewProductReq.getPrice());
        productEntity.setWeight(createNewProductReq.getWeight());
        productEntity.setDescription(createNewProductReq.getDescription());
        productEntity.setSlug(createNewProductReq.getProductName());
        productEntity.setActive(createNewProductReq.isActive());
        productEntity.setFeatured(createNewProductReq.isFeatured());
        productEntity.setProductCreatedDate(timeNow);
        productEntity.setProductUpdatedDate(timeNow);

        ProductsEntity newProduct = productsRepo.save(productEntity);

        List<ProductImagesEntity> newImages = uploadAndCreateProductImagesEntityList(newProduct.getProductId(), productImagesReq,primaryIndex);
        productImagesRepo.saveAll(newImages);

        GenericResponse response = new GenericResponse();
        response.setData(CustomizeResponseUtil.ReturnKeyValueWhenComplete("product_id", newProduct.getProductId()));
        response.setStatus(ResultCode.CREATED);
        return response;
    }

    @Transactional
    public GenericResponse updateProduct(UUID userId,UUID productId,
                                         CreateNewProductReq updateProductReq,
                                         List<MultipartFile> productImagesReq,
                                         int primaryIndex) throws ShopServiceApiException, IOException {

       if (updateProductReq == null)
            throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "Request body is missing.");

        if (productId == null)
            throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "Product id is missing.");

        if (productImagesReq.size() > 5)
            throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "You can reupload up to 5 images.");

        if (primaryIndex < 0 || productImagesReq.size() <= primaryIndex)
            throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "Invalid primary index.");

        UUID sellerId = userCheckTemp.getSellerIdByUserId(userId);

        if ( sellerId == null )
            throw new ShopUnAuthorizedException(ResultCode.UNAUTHORIZED, "You don't have permission to create new product.");

        ProductsEntity existingProduct = productsRepo.findById(productId)
                .orElseThrow(() -> new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "Product doesn't exists."));;

//        Image validation for reupload and delete
        List<ProductImagesResp> existImages = productImagesRepo.getProductImagesByProductId(productId);

        if(existImages.isEmpty())
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "Product images not found.");

        existingProduct.setProductName(updateProductReq.getProductName());
        existingProduct.setPrice(updateProductReq.getPrice());
        existingProduct.setWeight(updateProductReq.getWeight());
        existingProduct.setDescription(updateProductReq.getDescription());
        existingProduct.setSlug(updateProductReq.getProductName());
        existingProduct.setActive(updateProductReq.isActive());
        existingProduct.setFeatured(updateProductReq.isFeatured());
        existingProduct.setProductUpdatedDate(Instant.now());

        ProductsEntity newProduct = productsRepo.save(existingProduct);

//        delete old images and create new one in storage and database if reupload new images
        if(!productImagesReq.isEmpty()){
            Set<String> existImagesIds = existImages.stream()
                    .map(image -> productId + "/" + image.getProductImgPath())
                    .collect(Collectors.toSet());
            deleteProductImagesOutOfStorage(existImagesIds);

            productImagesRepo.deleteProductImagesEntitiesByProductsEntity_ProductId(productId);
            List<ProductImagesEntity> newImages = uploadAndCreateProductImagesEntityList(productId, productImagesReq, primaryIndex);
            productImagesRepo.saveAll(newImages);
        }


        GenericResponse response = new GenericResponse();
        response.setData(CustomizeResponseUtil.ReturnKeyValueWhenComplete("product_id", productId));
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }


    @Transactional
    public GenericResponse deleteProduct(UUID userId, UUID productId) throws ShopServiceApiException {
        if (productId == null)
            throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "Product id is missing.");

        UUID sellerId = userCheckTemp.getSellerIdByUserId(userId);

        if (sellerId == null)
            throw new ShopUnAuthorizedException(ResultCode.UNAUTHORIZED, "You don't have permission to delete product.");

        ProductsEntity existingProduct = productsRepo.findById(productId)
                .orElseThrow(() -> new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "Product doesn't exists."));

        List<ProductImagesResp> productImagesList = productImagesRepo.getProductImagesByProductId(productId);
        productsRepo.delete(existingProduct);

        Set<String> imageIdsList = productImagesList.stream()
                .map(image -> productId + "/" + image.getProductImgPath())
                .collect(Collectors.toSet());

        deleteProductImagesOutOfStorage(imageIdsList);

        GenericResponse response = new GenericResponse();
        response.setData(null);
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }

    private List<IProductHomeListItemResp> addPrefixProductImgPath (List<IProductHomeListItemResp> productsList) {
        productsList.forEach(product -> {
            if(product.getProductImgPath() != null)
                product.setProductImgPath(publicImageBaseUrl+product.getProductImgPath());
            else
                product.setProductImgPath(null);
        });
        return productsList;
    }

    private List<ProductImagesEntity> uploadAndCreateProductImagesEntityList(UUID productId, List<MultipartFile> productImagesReq, Integer primaryIndex) throws ShopServiceApiException, IOException {
        List<ProductImagesEntity> productImagesEntities = new ArrayList<>();
        ProductsEntity productEntity = new ProductsEntity();
        productEntity.setProductId(productId);

        for (MultipartFile imageReq : productImagesReq) {
            UUID productImgId = UUID.randomUUID();
            ProductImagesEntity imageEntity = new ProductImagesEntity();
            imageEntity.setProductImgId(productImgId);
            imageEntity.setProductImgPath(
                    productImgId + "." + Constants.CONTENT_TYPE_JPEG.split("/")[1]
            );
            imageEntity.setPrimary(
                    productImagesReq.indexOf(imageReq) == primaryIndex
            );
            imageEntity.setProductsEntity(productEntity);
            productImagesEntities.add(imageEntity);

            String productImgPath = productId + "/" + productImgId + "." + Constants.CONTENT_TYPE_JPEG.split("/")[1];
            uploadProductImagesToStorage(productImgPath, imageReq);
        }
        return productImagesEntities;

    }

    private void uploadProductImagesToStorage(String imagePath, MultipartFile imageData) throws ShopServiceApiException, IOException {
        supabaseStorageService.uploadImage(
                Constants.SUPABASE_PRODUCT_BUCKET_NAME,
                imagePath,
                processImageData(imageData),
                Constants.CONTENT_TYPE_JPEG
        );
    }

    private void deleteProductImagesOutOfStorage(Set<String> imageIdsList) {
        supabaseStorageService.deleteImage(
                Constants.SUPABASE_PRODUCT_BUCKET_NAME,
                imageIdsList
        );
    }
}

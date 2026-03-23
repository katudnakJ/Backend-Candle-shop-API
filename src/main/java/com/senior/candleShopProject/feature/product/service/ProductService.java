package com.senior.candleShopProject.feature.product.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.SupabaseService.SupabaseStorageService;
import com.senior.candleShopProject.common.UserCheckTemp;
import com.senior.candleShopProject.common.exception.*;
import com.senior.candleShopProject.common.utils.*;
import com.senior.candleShopProject.common.utils.dto.PaginationBuildResp;
import com.senior.candleShopProject.datasource.domain.products.IProductHomeListItemResp;
import com.senior.candleShopProject.datasource.entities.ProductImagesEntity;
import com.senior.candleShopProject.datasource.entities.ProductsEntity;
import com.senior.candleShopProject.datasource.repo.ProductImagesRepo;
import com.senior.candleShopProject.datasource.repo.ProductsRepo;
import com.senior.candleShopProject.datasource.domain.products.IProductResp;
import com.senior.candleShopProject.datasource.domain.products.ProductHomeListItemResp;
import com.senior.candleShopProject.feature.product.controller.dto.request.CreateNewProductReq;
import com.senior.candleShopProject.feature.product.controller.dto.request.UpdateProductReq;
import com.senior.candleShopProject.feature.product.controller.dto.response.ProductDetailResp;
import com.senior.candleShopProject.feature.product.controller.dto.response.ProductImagesResp;
import com.senior.candleShopProject.feature.product.controller.dto.response.ProductsBySearchResp;
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
    private final SupabaseStorageUtils supabaseStorageUtils;
    private final PaginationUtil paginationUtil;

    private final ProductsRepo productsRepo;
    private final ProductImagesRepo productImagesRepo;

    public GenericResponse getProductDetailById(UUID productId) throws ShopServiceApiException {

        IProductResp product = productsRepo.getProductById(productId);

        if (product == null)
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "Product not found.");

        List<ProductImagesResp> images = productImagesRepo.getProductImagesByProductId(productId);

        List<ProductImagesResp> productImages = images.stream().map(image -> {

            String imageUrl;
            if (image.getProductImgPath() != null) {
                imageUrl = supabaseStorageUtils.getProductImageUrl(
                        productId,
                        image.getProductImgPath()
                );
            } else {
                imageUrl = null;
            }

            ProductImagesResp resp = new ProductImagesResp();
            resp.setProductImgId(image.getProductImgId());
            resp.setProductImgPath(imageUrl);
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

    public GenericResponse getProductHomeListItem(int page, int size) throws ShopServiceApiException {

//      get product from repository
        List<IProductHomeListItemResp> prepareFeatureProducts = productsRepo.getProductHomeListItemByFeature(true);
        List<IProductHomeListItemResp> prepareAllProducts = productsRepo.getAllProductHomeList(size, page * size);

        if (prepareAllProducts.isEmpty())
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "Products not found.");

        List<IProductHomeListItemResp> featuredProducts = addPrefixProductImgPath(prepareFeatureProducts);
        List<IProductHomeListItemResp> productList = addPrefixProductImgPath(prepareAllProducts);

        Long totalCounts = productsRepo.count();

        PaginationBuildResp pagination = paginationUtil.buildPaginationResp(page, size, totalCounts);

        ProductHomeListItemResp productHomeListItemResp = new ProductHomeListItemResp();
        productHomeListItemResp.setFeaturedProducts(featuredProducts);
        productHomeListItemResp.setAllProducts(productList);
        productHomeListItemResp.setPage(page);
        productHomeListItemResp.setSize(size);
        productHomeListItemResp.setStartAt(pagination.getStartAt());
        productHomeListItemResp.setEndAt(pagination.getEndAt());
        productHomeListItemResp.setTotalProducts(pagination.getTotalItems());
        productHomeListItemResp.setHasNext(pagination.isHasNext());

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
        )throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "เกิดข้อผิดพลาดในการเพิ่มสินค้า กรุณาลองใหม่อีกครั้ง","Request body is missing or invalid primary index.");

        UUID sellerId = userCheckTemp.getSellerIdByUserId(userId);

        if ( sellerId == null)
            throw new ShopForbiddenException(ResultCode.FORBIDDEN, "คุณไม่มีสิทธิ์ในการเข้าถึง","User don't have permission to create new product.");

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
                                         UpdateProductReq updateProductReq,
                                         List<MultipartFile> productImagesReq) throws ShopServiceApiException {

        UUID sellerId = userCheckTemp.getSellerIdByUserId(userId);

        if (sellerId == null)
            throw new ShopForbiddenException(ResultCode.FORBIDDEN, "คุณไม่มีสิทธิ์ในการเข้าถึง","User don't have permission to update product.");

        if (productId == null)
            throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "Product id is missing.");

        if (productImagesReq == null)
            productImagesReq = new ArrayList<>();

        if (!productsRepo.existsById(productId))
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "Product doesn't exists.");

        Integer primaryIndex = updateProductReq.getPrimaryIndex();
        List<String> deleteImageIds = updateProductReq.getDeleteImageIds();

        if (updateProductReq.getExistIntoPrimary() != null) {
            if (!productImagesReq.isEmpty())
                throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "the setting defaultExistingImageId and image is unrelated.");
        } // Exist product image UUID from request

        if (primaryIndex != null && productImagesReq.isEmpty())
            throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "Primary index and image is unrelated.");

        List<ProductImagesResp> allProductImages = productImagesRepo.getProductImagesByProductId(productId);

//      Existing primary image from database
        ProductImagesResp existPrimaryImage = allProductImages.stream()
                .filter(ProductImagesResp::getIsPrimary)
                .findFirst()
                .orElseThrow(() -> new ShopBadRequestException(ResultCode.BAD_REQUEST, "Product doesn't have primary image."));

        for (String deleteImageId : deleteImageIds) {
            if (existPrimaryImage.getProductImgId().toString().equals(deleteImageId))
                throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "ไม่สามารถลบรูปหลักได้ กรุณาเปลี่ยนรูปหลักก่อนลบ","User can't delete primary image. Please set another image to primary before delete.");
        }

        if ((productImagesReq.size() + allProductImages.size() - deleteImageIds.size()) > 5)
            throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "จำกัดจำนวนรูปภาพไม่เกิน 5 รูป","User can reupload up to 5 images.");

        if (!deleteImageIds.isEmpty()) {
            List<UUID> deleteImageIdsUUID = deleteImageIds.stream()
                    .map(UUID::fromString)
                    .toList();

//            delete from database first
            productImagesRepo.deleteAllById(deleteImageIdsUUID);
//            delete from storage
            Set<String> deleteImageIdsPath = allProductImages.stream()
                    .filter(image -> deleteImageIds.contains(image.getProductImgId().toString()))
                    .map(image -> productId + "/" + image.getProductImgPath())
                    .collect(Collectors.toSet());
            deleteProductImagesOutOfStorage(deleteImageIdsPath);
        }

//        กรณีที่มีการอัพโหลดรูปใหม่พร้อมกับการตั้ง primary index ให้กับรูปใหม่
        if (!productImagesReq.isEmpty()) {
            setAllNewProductImages(updateProductReq, productImagesReq, existPrimaryImage, productId);
        }
        if (updateProductReq.getExistIntoPrimary() != null) {
            if (!existPrimaryImage.getProductImgId().toString().equals(updateProductReq.getExistIntoPrimary())){
                productImagesRepo.unsetPrimaryImage(existPrimaryImage.getProductImgId());
                productImagesRepo.setPrimaryImage(UUID.fromString(updateProductReq.getExistIntoPrimary()));
            }

        }

//        and then save Product detail to database
        ProductsEntity productEntity = productsRepo.findProductsEntityByProductId(productId);
        productEntity.setProductName(updateProductReq.getProductName());
        productEntity.setPrice(updateProductReq.getPrice());
        productEntity.setWeight(updateProductReq.getWeight());
        productEntity.setDescription(updateProductReq.getDescription());
        productEntity.setSlug(updateProductReq.getProductName());
        productEntity.setActive(updateProductReq.isActive());
        productEntity.setFeatured(updateProductReq.isFeatured());
        productEntity.setProductUpdatedDate(Instant.now());

        productsRepo.save(productEntity);

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
            throw new ShopForbiddenException(ResultCode.FORBIDDEN, "คุณไม่ได้รับอนุญาตให้เข้าถึงหน้านี้", "User don't have permission to delete product.");

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

    public GenericResponse searchProducts(String query, int page, int size) {
        List<IProductHomeListItemResp> searchResults = productsRepo.searchProductsByName(query, size, page * size);

        Long totalCounts = productsRepo.countByProductNameContainingIgnoreCase(query);
        PaginationBuildResp pagination = paginationUtil.buildPaginationResp(page, size, totalCounts);

        ProductsBySearchResp productHomeListItemResp = new ProductsBySearchResp();
        productHomeListItemResp.setProducts(addPrefixProductImgPath(searchResults));
        productHomeListItemResp.setPage(page);
        productHomeListItemResp.setSize(size);
        productHomeListItemResp.setStartAt(pagination.getStartAt());
        productHomeListItemResp.setEndAt(pagination.getEndAt());
        productHomeListItemResp.setTotalProducts(pagination.getTotalItems());
        productHomeListItemResp.setHasNext(pagination.isHasNext());

        GenericResponse response = new GenericResponse();
        response.setData(productHomeListItemResp);
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }

//    Extract function for more readability and reuse in other function if needed

    private List<IProductHomeListItemResp> addPrefixProductImgPath (List<IProductHomeListItemResp> productsList) {
        productsList.forEach(product -> {
            if(product.getProductImgPath() != null)
                product.setProductImgPath(
                        supabaseStorageUtils.getProductImageUrl(
                                product.getProductId(),
                                product.getProductImgPath()
                        )
                );
            else
                product.setProductImgPath(null);
        });
        return productsList;
    }

    private List<ProductImagesEntity> uploadAndCreateProductImagesEntityList(UUID productId, List<MultipartFile> productImagesReq, Integer primaryIndex) throws IOException {
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

    private void uploadProductImagesToStorage(String imagePath, MultipartFile imageData) throws IOException {
        supabaseStorageService.uploadFile(
                Constants.SUPABASE_PRODUCT_BUCKET_NAME,
                imagePath,
                processImageData(imageData),
                Constants.CONTENT_TYPE_JPEG
        );
    }

    private void deleteProductImagesOutOfStorage(Set<String> imageIdsList) {
        supabaseStorageService.deleteFiles(
                Constants.SUPABASE_PRODUCT_BUCKET_NAME,
                imageIdsList
        );
    }

    private void setAllNewProductImages(UpdateProductReq  updateProductReq,
                                        List<MultipartFile> productImagesReq,
                                        ProductImagesResp existPrimaryImage,
                                        UUID productId) throws ShopServiceApiException {
        if (updateProductReq.getPrimaryIndex() != null
                && updateProductReq.getPrimaryIndex() >= productImagesReq.size())
            throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "เกิดข้อผิดพลาดในการอัปโหลดรูปภาพ","Invalid primary index.");


        ProductsEntity productEntity = new ProductsEntity();
        productEntity.setProductId(productId);

//        set all new images and primary image in database and storage
        for (int i = 0; i < productImagesReq.size(); i++) {
//          use primary index to check if it is primary or not
            boolean isThisNewPrimary = (
                    updateProductReq.getPrimaryIndex() != null && updateProductReq.getPrimaryIndex() == i
            );

            ProductImagesEntity newPrimaryImageEntity = new ProductImagesEntity();
            newPrimaryImageEntity.setProductImgId(UUID.randomUUID());

//            set primary image logic
            if (isThisNewPrimary) {
                newPrimaryImageEntity.setPrimary(true);
                if (existPrimaryImage != null) {
                    productImagesRepo.unsetPrimaryImage(existPrimaryImage.getProductImgId());
                }
            }else{
                newPrimaryImageEntity.setPrimary(false);
            }

            newPrimaryImageEntity.setProductImgPath(
                   newPrimaryImageEntity.getProductImgId() + "." + Constants.CONTENT_TYPE_JPEG.split("/")[1]
            );
            newPrimaryImageEntity.setProductsEntity(productEntity);

            productImagesRepo.save(newPrimaryImageEntity);

            try {
                supabaseStorageUtils.uploadProductImage(
                        productId,
                        newPrimaryImageEntity.getProductImgId(),
                        productImagesReq.get(i)
                );
            } catch (IOException e) {
                throw new ShopServiceApiException(ResultCode.INTERNAL_SERVER_ERROR, "เกิดข้อผิดพลาดในการอัปโหลดรูปภาพ", "Failed to upload primary image.");
            }

        }
    }
}

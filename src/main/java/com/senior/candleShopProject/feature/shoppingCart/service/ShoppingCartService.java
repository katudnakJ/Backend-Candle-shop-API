package com.senior.candleShopProject.feature.shoppingCart.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopDataNotFoundException;
import com.senior.candleShopProject.common.exception.ShopForbiddenException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.common.utils.CustomizeResponseUtil;
import com.senior.candleShopProject.datasource.entities.ProductsEntity;
import com.senior.candleShopProject.datasource.entities.ShoppingCartEntity;
import com.senior.candleShopProject.datasource.domain.shoppingCart.IAllItemsShoppingCartResp;
import com.senior.candleShopProject.datasource.entities.ShoppingCartItemsEntity;
import com.senior.candleShopProject.datasource.repo.ShoppingCartItemsRepo;
import com.senior.candleShopProject.datasource.repo.ShoppingCartRepo;
import com.senior.candleShopProject.feature.shoppingCart.controller.dto.request.AddShoppingCartItemReq;
import com.senior.candleShopProject.feature.shoppingCart.controller.dto.request.DeleteShoppingCartItemReq;
import com.senior.candleShopProject.feature.shoppingCart.controller.dto.response.ShoppingCartItemsList;
import com.senior.candleShopProject.feature.shoppingCart.controller.dto.response.ShoppingCartResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingCartService {

    @Value("${supabase.storage.public-image-base-url}")
    private String publicImageBaseUrl;

    private final ShoppingCartRepo shoppingCartRepo;

    private final ShoppingCartItemsRepo shoppingCartItemsRepo;

    public GenericResponse getShoppingCart(UUID userId, int page, int size) throws ShopServiceApiException {

        List<IAllItemsShoppingCartResp> iShoppingCartResps = shoppingCartRepo.getAllItemsFromShoppingCartByUserId(userId, size, page * size);

        if (iShoppingCartResps.isEmpty()){
            GenericResponse response = new GenericResponse();
            response.setData(null);
            response.setStatus(ResultCode.SUCCESS);
            return response;
        }

        List<ShoppingCartItemsList> shoppingCartItemsList = getShoppingCartItemsList(iShoppingCartResps);

        ShoppingCartResp shoppingCartResp = new ShoppingCartResp();
        shoppingCartResp.setShoppingCartId(iShoppingCartResps.get(0).getShoppingCartId());
        shoppingCartResp.setCartItems(shoppingCartItemsList);

        GenericResponse response = new GenericResponse();
        response.setData(shoppingCartResp);
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }

    @Transactional
    public GenericResponse addShoppingCartItem(UUID userId, AddShoppingCartItemReq addShoppingCartItemReq) throws ShopServiceApiException {
        UUID shoppingCartId = shoppingCartRepo.getShoppingCartIdByUserId(userId);

        if (shoppingCartId == null)
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "Shopping cart not found.");

        UUID productId = UUID.fromString(addShoppingCartItemReq.getProductId());
        Optional<ShoppingCartItemsEntity> existCartItems = shoppingCartItemsRepo
                .findShoppingCartItemsEntitiesByShoppingCartEntity_ShoppingCartIdAndProductsEntity_ProductId
                        (shoppingCartId, productId);

//      already have the same product in cart, update quantity
        ShoppingCartItemsEntity shoppingCartItemsEntity;
        if(existCartItems.isPresent()){
            shoppingCartItemsEntity = existCartItems.get();
            shoppingCartItemsEntity.setQuantity(addShoppingCartItemReq.getQuantity());
        }else{
            shoppingCartItemsEntity = getShoppingCartItemsEntity(addShoppingCartItemReq, shoppingCartId);
            shoppingCartItemsEntity.setCreatedAt(Instant.now());
        }
        ShoppingCartItemsEntity savedData = shoppingCartItemsRepo.save(shoppingCartItemsEntity);

        GenericResponse response = new GenericResponse();
        response.setData(CustomizeResponseUtil.ReturnKeyValueWhenComplete(savedData.getShoppingCartItemId()));
        response.setStatus(ResultCode.SUCCESS);

        return response;
    }

    @Transactional
    public GenericResponse deleteShoppingCartItem(UUID userId, DeleteShoppingCartItemReq deleteShoppingCartItemReq) throws ShopServiceApiException {
        UUID shoppingCartId = shoppingCartRepo.getShoppingCartIdByUserId(userId);
        UUID shoppingCartItemId = UUID.fromString(deleteShoppingCartItemReq.getShoppingCartItemId());

        if(!shoppingCartItemsRepo.existsByShoppingCartItemId(shoppingCartItemId))
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "Shopping cart item is not exists.");

        if (shoppingCartId == null)
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "Shopping cart not found.");

        if (!shoppingCartId.toString().equalsIgnoreCase(deleteShoppingCartItemReq.getShoppingCartId()))
            throw new ShopForbiddenException(ResultCode.FORBIDDEN, "You don't have permission to delete this item.");

        shoppingCartItemsRepo.deleteById(shoppingCartItemId);

        GenericResponse response = new GenericResponse();
        response.setData(null);
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }

    private List<ShoppingCartItemsList> getShoppingCartItemsList(List <IAllItemsShoppingCartResp> iShoppingCartResp) {

        List<ShoppingCartItemsList> shoppingCartItemsList = new ArrayList<>();

        for (IAllItemsShoppingCartResp cart : iShoppingCartResp) {
            ShoppingCartItemsList cartItemsList = new ShoppingCartItemsList();
            cartItemsList.setShoppingCartItemId(cart.getShoppingCartItemId());
            cartItemsList.setProductId(cart.getProductId());
            cartItemsList.setQuantity(cart.getQuantity());
            cartItemsList.setProductName(cart.getProductName());
            cartItemsList.setPrice(cart.getPrice());
            cartItemsList.setProductSlug(cart.getProductSlug());
            if(cart.getProductImgPath() == null)
                cartItemsList.setProductImgPath(null);
            else
                cartItemsList.setProductImgPath(publicImageBaseUrl + cart.getProductImgPath());

            shoppingCartItemsList.add(cartItemsList);
        }
        return shoppingCartItemsList;

    }

    private static ShoppingCartItemsEntity getShoppingCartItemsEntity(AddShoppingCartItemReq shoppingCartItemReq, UUID shoppingCartId) {
        ShoppingCartEntity shoppingCartEntity = new ShoppingCartEntity();
        shoppingCartEntity.setShoppingCartId(shoppingCartId);

        UUID productId = UUID.fromString(shoppingCartItemReq.getProductId());

        ProductsEntity productsEntity = new ProductsEntity();
        productsEntity.setProductId(productId);

        ShoppingCartItemsEntity shoppingCartItemsEntity = new ShoppingCartItemsEntity();
        shoppingCartItemsEntity.setShoppingCartEntity(shoppingCartEntity);
        shoppingCartItemsEntity.setProductsEntity(productsEntity);
        shoppingCartItemsEntity.setQuantity(shoppingCartItemReq.getQuantity());
        return shoppingCartItemsEntity;
    }
}


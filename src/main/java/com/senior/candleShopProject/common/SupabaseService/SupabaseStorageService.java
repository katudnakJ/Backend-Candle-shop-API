package com.senior.candleShopProject.common.SupabaseService;

import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.SupabaseService.Dto.SignedUrlResp;
import com.senior.candleShopProject.common.exception.ShopDataNotFoundException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupabaseStorageService {

    @Value("${supabase.storage.base-url}")
    private  String baseUrl;

    private final WebClient supabaseWebClient;

     private String createSignedImageUrl(String bucketName, String imagePath, int expiresInSeconds) {

         String url = "/storage/v1/object/sign/" + bucketName + "/" + imagePath;
        return supabaseWebClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("expiresIn", expiresInSeconds))
                .retrieve()
                .bodyToMono(SignedUrlResp.class)
                .flatMap(response -> Mono.just(response.getUrl()))
                .onErrorResume(
                        ex -> {
                            log.error("Error creating signed image URL", ex);
                            if(ex.toString().contains("404"))
                                return Mono.error(new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "Image not found in storage."));
                            return Mono.error(ex);
                        }
                )
                .onErrorMap( ex -> {
                    log.error("Error creating signed image URL", ex);
                    return ex;
                })
                .block();
    }

    public String getSignedImageUrl(String bucketName, String imagePath, int expiresInSeconds) throws ShopServiceApiException {
        String signedImage =createSignedImageUrl(bucketName, imagePath, expiresInSeconds);

        if (signedImage == null)
            return null;
        return baseUrl + "/storage/v1" + signedImage;
    }

    public void uploadImage(String bucketName, String imagePath, byte[] imageData, String contentType) {
        String uploadUrl = baseUrl + "/storage/v1/object/" + bucketName + "/" + imagePath;

        supabaseWebClient.post()
                .uri(uploadUrl)
                .contentType(MediaType.parseMediaType(contentType))
                .header("x-upsert", "true")
                .bodyValue(imageData)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class).flatMap(errorBody -> {
                            log.error("Supabase Storage Error: {}", errorBody);
                            return Mono.error(new ShopServiceApiException(ResultCode.INTERNAL_SERVER_ERROR, "Storage upload failed."));
                        }))
                .bodyToMono(Void.class)
                .onErrorMap(ex -> {
                    log.error("Upload failed", ex);
                    return new ShopServiceApiException(ResultCode.INTERNAL_SERVER_ERROR, "Image upload failed");
                })
                .block();
    }

    public void deleteImage(String bucketName, Set<String> imagePaths) {
        String deleteUrl = baseUrl + "/storage/v1/object/" + bucketName;

        Map<String, Set<String>> body = new HashMap<>();
        body.put("prefixes", imagePaths);

        supabaseWebClient.method(HttpMethod.DELETE)
                .uri(deleteUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class).flatMap(errorBody -> {
                            log.error("Supabase Error: {}", errorBody);
                            return Mono.error(new RuntimeException("Storage deletion failed : " + errorBody));
                        }))
                .bodyToMono(String.class)
                .doOnSuccess(s -> log.info("Delete Successful: {}", s))
                .block();
    }

}

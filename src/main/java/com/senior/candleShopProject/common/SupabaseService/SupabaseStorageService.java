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
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupabaseStorageService {

    @Value("${supabase.storage.base-url}")
    private  String baseUrl;

    private final WebClient supabaseWebClient;

     private String createSignedFileUrl(String bucketName, String filePath, int expiresInSeconds) {

         String url = "/storage/v1/object/sign/" + bucketName + "/" + filePath;
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

    public String getSignedFileUrl(String bucketName, String filePath, int expiresInSeconds) throws ShopServiceApiException {
        String signedImage = createSignedFileUrl(bucketName, filePath, expiresInSeconds);

        if (signedImage == null)
            return null;
        return baseUrl + "/storage/v1" + signedImage;
    }

    public void uploadFile(String bucketName, String filePath, byte[] file, String contentType) {
        String uploadUrl = baseUrl + "/storage/v1/object/" + bucketName + "/" + filePath;

        supabaseWebClient.post()
                .uri(uploadUrl)
                .contentType(MediaType.parseMediaType(contentType))
                .header("x-upsert", "true")
                .bodyValue(file)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class).flatMap(errorBody -> {
                            log.error("Supabase Storage Error: {}", errorBody);
                            return Mono.error(new ShopServiceApiException(ResultCode.INTERNAL_SERVER_ERROR, null, "Storage upload failed."));
                        }))
                .bodyToMono(Void.class)
                .onErrorMap(ex -> {
                    log.error("Upload failed", ex);
                    return new ShopServiceApiException(ResultCode.INTERNAL_SERVER_ERROR, null, "Image upload failed");
                })
                .block();
    }

    public void deleteFiles(String bucketName, Set<String> filePaths) {
        String deleteUrl = baseUrl + "/storage/v1/object/" + bucketName;

        Map<String, Set<String>> body = new HashMap<>();
        body.put("prefixes", filePaths);

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

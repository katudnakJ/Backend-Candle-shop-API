package com.senior.candleShopProject.common.LineService;

import com.senior.candleShopProject.common.LineService.dto.LineProfileResp;
import com.senior.candleShopProject.common.LineService.dto.LineVerifyResp;
import com.senior.candleShopProject.common.utils.Constants;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;

import java.net.URI;

@Service
public class LineAPIClient {

    private final WebClient lineWebClient =
            WebClient.builder()
            .baseUrl(Constants.LINE_BASE_URL)
            .build();

    public final LineVerifyResp getVerifyResp(String token){
        return lineWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(Constants.LINE_TOKEN_VERIFY_URL)
                        .queryParam("access_token", token)
                        .build()
                )
                .retrieve()
                .bodyToMono(LineVerifyResp.class)
                .block();
    }

    public LineProfileResp getProfileResp(String token) {
        WebClient.RequestHeadersSpec<?> req =
                (RequestHeadersSpec<?>) lineWebClient.get()
                        .uri(Constants.LINE_TOKEN_GET_PROFILE_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        return req
                .retrieve()
                .bodyToMono(LineProfileResp.class)
                .block();
    }

    public final Boolean isTokenExpired(String token){
        LineVerifyResp verifyResp = getVerifyResp(token);
        if(verifyResp == null) return true;
        return verifyResp.getExpires_in() <= 0;
    }
}

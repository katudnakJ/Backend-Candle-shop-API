package com.senior.candleShopProject.common.LineService;

import com.senior.candleShopProject.common.LineService.dto.LineMessage;
import com.senior.candleShopProject.common.LineService.dto.LineProfileResp;
import com.senior.candleShopProject.common.LineService.dto.LinePushMessageReq;
import com.senior.candleShopProject.common.LineService.dto.LineVerifyResp;
import com.senior.candleShopProject.common.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;

import java.util.List;

@Service
@Slf4j
public class LineAPIClient {

    @Value("${line.channel.access-token}")
    private String messagingApiToken;

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

    public void sendPushMessage(String userId, String message) {

        LineMessage lineMessage = new LineMessage("text", message);

        LinePushMessageReq linePushMessageReq = new LinePushMessageReq();
        linePushMessageReq.setTo(userId);
        linePushMessageReq.setMessages(List.of(lineMessage));

        try {
            lineWebClient.post()
                    .uri(Constants.LINE_PUSH_MESSAGE_URL)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + messagingApiToken)
                    .bodyValue(linePushMessageReq)
                    .retrieve()
                    .onStatus(
                            status -> !status.is2xxSuccessful(),
                            response -> response.bodyToMono(String.class)
                                    .map(body -> new RuntimeException("LINE error: " + body))
                    )
                    .bodyToMono(Void.class)
                    .block();

        } catch (Exception e) {
            log.error("Send LINE push message failed | userId={} message={}", userId, message, e);
        }
    }
}

package com.senior.candleShopProject.common.LineService;

import org.springframework.stereotype.Service;

@Service
public class LineMessageService {

    private final LineAPIClient lineAPIClient;

    public LineMessageService(LineAPIClient lineAPIClient) {
        this.lineAPIClient = lineAPIClient;
    }

        public void pushMessage(String lineUserId, String message) {
            lineAPIClient.sendPushMessage(lineUserId, message);
        }
}

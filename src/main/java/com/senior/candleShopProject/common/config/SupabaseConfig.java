package com.senior.candleShopProject.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SupabaseConfig {

    @Value("${supabase.storage.base-url}")
    private String storageBaseUrl;

    @Value("${supabase.secret.service-role-key}")
    private String secretRoleKey;

    @Bean
    public WebClient supabaseWebClient() {
        return WebClient.builder()
                .baseUrl(storageBaseUrl)
                .defaultHeader("apikey", secretRoleKey)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + secretRoleKey)
                .build();
    }
}

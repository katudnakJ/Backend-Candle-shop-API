package com.senior.candleShopProject.common.config;

import com.senior.candleShopProject.common.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration

public class ApplicationConfig implements WebMvcConfigurer {
    private final AuthInterceptor authInterceptor;

    public ApplicationConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/v1/login", "/v1/login/**");
    }
}

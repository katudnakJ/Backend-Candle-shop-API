package com.senior.candleShopProject.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Scented Candle Shop API 🕯️")
                        .version("1.0.0")
                        .description("""
                                ### ระบบจัดการร้านเทียนหอม (Senior Project)
                                
                                \uD83D\uDC7B ถ้าหากต้องการ Response ของแต่ละ API แบบเต็ม\s
                                สามารถดูได้ที่นี่: \
                                [API Documentation (Excel)](https://silpakorn-my.sharepoint.com/:x:/g/personal/katudnak_j_su_ac_th/IQAMpU6QUi_cSZtBqQaA-Z0BAWzsvpoVbGWu5y8M6vnatiA?e=D8zPhr&nav=MTVfezlEQjc4QTgwLUQ1NDEtNDBEMi05RUZFLUEwQUJBRjQ1RDg3MX0)"""));
    }
}
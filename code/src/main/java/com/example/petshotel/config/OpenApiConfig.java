package com.example.petshotel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI petsHotelOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("PetsHotel API")
                        .version("v1")
                        .description("""
                                REST API ของระบบโรงแรมสัตว์เลี้ยง PetsHotel

                                **วิธีทดสอบ endpoint ที่ต้องล็อกอิน:** เปิดหน้า `/login` \
                                ในเบราว์เซอร์เดียวกันแล้วเข้าสู่ระบบก่อน จากนั้นกลับมาหน้านี้แล้วกด "Try it out"
                                """));
    }
}
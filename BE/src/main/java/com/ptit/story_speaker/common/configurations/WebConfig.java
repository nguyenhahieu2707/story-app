package com.ptit.story_speaker.common.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Áp dụng cho toàn bộ API
                .allowedOrigins("*") // Cho phép mọi nguồn (domain). Trong production nên thay bằng domain cụ thể (vd: http://localhost:3000)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH") // Các method được phép
                .allowedHeaders("*") // Cho phép mọi header (Authorization, Content-Type,...)
                .exposedHeaders("Authorization") // Cho phép FE đọc được header trả về (nếu cần)
                .maxAge(3600); // Cache cấu hình CORS trong 1 giờ để giảm tải request OPTIONS
    }
}
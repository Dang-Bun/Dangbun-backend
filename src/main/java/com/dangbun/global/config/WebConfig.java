package com.dangbun.global.config;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                    "http://localhost:5173", // 프론트 로컬 주소 
                    "https://dangbun-frontend-mu.vercel.app", // 프론트 배포 주소 
                    "https://dangbun.o-r.kr" // 백엔드 배포 주소 
                )
                .allowedMethods("GET","POST","PATCH","DELETE", "PUT")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}

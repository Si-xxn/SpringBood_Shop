package com.shop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${uploadPath}")
    String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 웹 브라우져에 입력하는 url에 /images로 시작하는 경우 로컬 컴퓨터에 저장된 파일 읽어올 경로
        // service.FileService에 파일 처리용 클래스 생성
        registry.addResourceHandler("/images/**")
                .addResourceLocations(uploadPath);
    }
}

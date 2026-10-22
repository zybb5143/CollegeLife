package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 * 主要用于配置 CORS 跨域访问，决前后端分离架构中的跨域解问题
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * CORS 跨域配置
     *
     * @param registry CORS 注册表
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(CorsConfigConstants.ALLOWED_ORIGINS.toArray(new String[0]))
                .allowedMethods(CorsConfigConstants.ALLOWED_METHODS.toArray(new String[0]))
                .allowedHeaders(CorsConfigConstants.ALLOWED_HEADERS.toArray(new String[0]))
                .allowCredentials(CorsConfigConstants.ALLOW_CREDENTIALS)
                .maxAge(CorsConfigConstants.MAX_AGE);
    }

    /**
     * 静态资源映射配置
     * <p>
     * /uploads/** 直接映射到磁盘上的上传目录（file:），供帖子图片等直接访问；
     * 下载/预览由 DownloadController 处理，不需要在这里重复注册（且
     * classpath:/static/uploads/ 在打包后为只读，运行时新上传的文件不在其中）。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = "file:" + System.getProperty("user.dir") + "/src/main/resources/static/uploads/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}

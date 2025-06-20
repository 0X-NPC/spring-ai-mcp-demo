package com.joyhubs.ai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 *
 * @author OxNPC
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    public static final String DEFAULT_PAGE = "index.html";

    /**
     * 添加视图控制器，将根路径 "/" 和"/index" 直接映射到 "index.html"
     *
     * @param registry 视图控制器注册表
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 根路径 "/"、"/index"映射到 "index.html"
        registry.addViewController("/").setViewName(DEFAULT_PAGE);
        registry.addViewController("/index").setViewName(DEFAULT_PAGE);
    }
}

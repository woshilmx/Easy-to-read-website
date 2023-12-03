package com.lmx.project.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${resource.path}")
    private String resource;

    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        System.out.println(resource);
        registry.addResourceHandler("/resource/**").addResourceLocations("file:" + resource);
    }
}

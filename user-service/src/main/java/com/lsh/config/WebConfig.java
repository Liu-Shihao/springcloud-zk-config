package com.lsh.config;

import com.lsh.interceptor.AuthenticationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/24 14:46
 * @Desc:
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthenticationInterceptor());
//        registry.addInterceptor(new OldLoginInterceptor()).addPathPatterns("/admin/oldLogin");
//        registry.addInterceptor(new	AdminInterceptor()).addPathPatterns("/admin/*").excludePathPatterns("/admin/oldLogin");
    }
}


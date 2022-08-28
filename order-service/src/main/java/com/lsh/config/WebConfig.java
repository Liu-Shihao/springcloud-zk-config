package com.lsh.config;

import com.lsh.utils.interceptor.AuthenticationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/24 14:46
 * @Desc:
 * registry.addInterceptor(new AuthenticationInterceptor()).addPathPatterns("/admin/oldLogin");
 * registry.addInterceptor(new AuthenticationInterceptor()).addPathPatterns("/admin/*").excludePathPatterns("/admin/oldLogin");
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthenticationInterceptor());
    }
}


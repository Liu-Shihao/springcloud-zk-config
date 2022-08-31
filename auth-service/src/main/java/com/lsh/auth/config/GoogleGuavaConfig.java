package com.lsh.auth.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/26 09:50
 * @Desc: 本地缓存
 */
@Slf4j
@Configuration
public class GoogleGuavaConfig {

    @Bean
    public LoadingCache<String, Set<String>> localCache(MyCacheLoader myCacheLoader){
        return CacheBuilder.newBuilder()
                .initialCapacity(10000)    //设置缓存初始大小
                .maximumSize(10000)       //设置最大容量
                .concurrencyLevel(5)    //并发数设置
                .expireAfterWrite(300, TimeUnit.SECONDS) //缓存过期时间    5分钟
                .expireAfterAccess(60,TimeUnit.SECONDS) // 此缓存对象经过多少秒没有被访问则过期。
                .recordStats() //统计缓存命中率
                .build(myCacheLoader);
    }

}

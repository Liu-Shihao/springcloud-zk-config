package com.lsh.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@Configuration
public class CuratorConfig {

    @Value("${zookeeper.address}")
    public String address ;

    @Bean("curatorClient")
    public CuratorFramework curatorClient() throws Exception {
        // baseSleepTimeMs:初始的sleep时间，用于计算之后的每次重试的sleep时间 ; maxRetries: 最大重试次数
        RetryPolicy retryPolicy  = new ExponentialBackoffRetry(1000,3);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                //连接地址  集群用,隔开
                .connectString(address)
                .connectionTimeoutMs(5000) //连接超时时间
                .sessionTimeoutMs(3000)//会话超时时间
                .retryPolicy(retryPolicy)//设置重试机制
                .build();
        client.start();
        return client;
    }

}

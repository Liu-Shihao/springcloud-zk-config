package com.citi.zk.config;

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
        RetryPolicy retryPolicy  = new ExponentialBackoffRetry(1000,3);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                //连接地址  集群用,隔开
                .connectString(address)
                .connectionTimeoutMs(50000)
                .sessionTimeoutMs(30000)
                .retryPolicy(retryPolicy)
                .build();
        client.start();
        return client;
    }

}

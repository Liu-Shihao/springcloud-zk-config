package com.lsh.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/14 10:37
 * @Desc:
 */
@Configuration
@ConfigurationProperties(prefix = "zookeeper")
@Data
public class ZookeeperProperties {

    public String address;

    public int sessionTimeOut;

    public int connectionTimeoutMs;

    public int sleepMsBetweenRetry;

    public int maxRetries;

}

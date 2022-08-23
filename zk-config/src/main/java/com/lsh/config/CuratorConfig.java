package com.lsh.config;

import com.lsh.watch.ZookeeperWatches;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/15 10:49
 * @Desc:
 */
@Slf4j
@Component
@Configurable
public class CuratorConfig {

    @Autowired
    ZookeeperProperties zookeeperProperties;

    @Bean("curatorClient")
    public CuratorFramework curatorClient() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.builder()
                //连接地址  集群用,隔开
                .connectString(zookeeperProperties.getAddress())
                .connectionTimeoutMs(zookeeperProperties.getConnectionTimeoutMs())
                //会话超时时间
                .sessionTimeoutMs(zookeeperProperties.getSessionTimeOut())
                //设置重试机制
                .retryPolicy(new ExponentialBackoffRetry(zookeeperProperties.getSleepMsBetweenRetry(),zookeeperProperties.getMaxRetries()))
                //设置命名空间 在操作节点的时候，会以这个为父节点
//                .namespace(zookeeperProperties.getNamespace())
                .build();
        client.start();
        //注册监听器
        ZookeeperWatches watches = new ZookeeperWatches(client);
        watches.znodeWatcher();
        watches.znodeChildrenWatcher();
        return client;
    }




}

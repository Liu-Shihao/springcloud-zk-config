package com.citi.zk.config;

import com.citi.zk.watch.DefaultWatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CountDownLatch;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/11 14:35
 * @Desc:
 */
@Slf4j
@Configuration
public class ZookeeperConfig {

    @Autowired
    MyWatch myWatch;

    @Autowired
    ZookeeperProperties zookeeperProperties;

    @Bean("zkClient")
    public ZooKeeper zooKeeper() throws Exception {
        ZooKeeper zooKeeper = null;
        CountDownLatch countDownLatch = new CountDownLatch(1);
        zooKeeper = new ZooKeeper(zookeeperProperties.getAddress(),zookeeperProperties.getSessionTimeOut() , new DefaultWatch(countDownLatch,zooKeeper));
        countDownLatch.await();//等待zk客户端连接
        log.info("==========Zookeeper Connection Success !==========");

        return zooKeeper;
    }


}

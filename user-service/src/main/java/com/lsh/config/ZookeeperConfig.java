package com.lsh.config;

import com.lsh.watch.DefaultWatch;
import com.lsh.watch.WatchCallBack;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
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
    ZookeeperProperties zookeeperProperties;

    @Bean("zkClient")
    public ZooKeeper zooKeeper() throws IOException {
        ZooKeeper zooKeeper = null;
        try {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            zooKeeper = new ZooKeeper(zookeeperProperties.getAddress(),zookeeperProperties.getSessionTimeOut() , new DefaultWatch(countDownLatch,zooKeeper));
            countDownLatch.await();
            log.info("Zookeeper连接成功!");
            log.info("拉去本地缓存...");
            WatchCallBack watchCallBack = new WatchCallBack(zooKeeper);
            watchCallBack.aWait("/test");
        }catch (Exception e){
            log.error("创建zookeeper客户端失败:" + e.getMessage());
        }
        return zooKeeper;
    }


}

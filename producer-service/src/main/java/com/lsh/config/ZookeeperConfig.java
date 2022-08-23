package com.lsh.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/11 14:35
 * @Desc:
 */
@Slf4j
@Configurable
@Component
public class ZookeeperConfig {

    @Autowired
    ZookeeperProperties zookeeperProperties;

    @Bean("zkClient")
    public ZooKeeper zooKeeper() throws IOException {
        ZooKeeper zooKeeper = null;
        try {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            zooKeeper = new ZooKeeper(zookeeperProperties.getAddress(),zookeeperProperties.getSessionTimeOut() , new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if(Event.KeeperState.SyncConnected==watchedEvent.getState()){
                        log.info("====客户端建立连接!====");
                        countDownLatch.countDown();//如果收到了服务端的响应事件,连接成功
                    }
                }
            });
            countDownLatch.await();
        }catch (Exception e){
            log.error("创建zookeeper客户端失败:" + e.getMessage());
        }
        return zooKeeper;
    }
}

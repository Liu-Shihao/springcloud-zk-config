package com.lsh.utils.config;

import com.hazelcast.core.HazelcastInstance;
import com.lsh.utils.watch.DefaultWatch;
import com.lsh.utils.watch.MyWatch;
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

    @Autowired
    HazelcastInstance hazelcastClient;


    @Bean("zkClient")
    public ZooKeeper zooKeeper() throws IOException {
        ZooKeeper zooKeeper = null;
        try {
            // 1.连接zk
            CountDownLatch countDownLatch = new CountDownLatch(1);
            zooKeeper = new ZooKeeper(zookeeperProperties.getAddress(),zookeeperProperties.getSessionTimeOut() , new DefaultWatch(countDownLatch,zooKeeper));
            countDownLatch.await();
            log.info("==========Zookeeper连接成功!==========");
            //2.从zk中获取权限数据
            // 获得所有的user节点
            MyWatch myWatch = new MyWatch(zooKeeper);
            myWatch.loadingLocalCache();
        }catch (Exception e){
            log.error("创建zookeeper客户端失败:" + e.getMessage());
        }
        return zooKeeper;
    }


}

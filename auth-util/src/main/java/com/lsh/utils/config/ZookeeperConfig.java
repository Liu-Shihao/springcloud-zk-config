package com.lsh.utils.config;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.lsh.constant.ZKConstant;
import com.lsh.utils.watch.DefaultWatch;
import com.lsh.utils.watch.WatchCallBack;
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
            CountDownLatch countDownLatch = new CountDownLatch(1);
            zooKeeper = new ZooKeeper(zookeeperProperties.getAddress(),zookeeperProperties.getSessionTimeOut() , new DefaultWatch(countDownLatch,zooKeeper));
            countDownLatch.await();
            log.info("==========Zookeeper连接成功!==========");
            System.out.println("第一次读取本地缓存："+GoogleGuava.localCache.get(ZKConstant.CACHE_MAP_USER_KEY));
            //从Hazelcast中把数据拉取到本地缓存
            IMap<String, Object> hazelcastCache = hazelcastClient.getMap(ZKConstant.CACHE_MAP_USER);
            GoogleGuava.localCache.put(ZKConstant.CACHE_MAP_USER_KEY,hazelcastCache.get(ZKConstant.CACHE_MAP_USER_KEY));

            //监听/auth-service  version
            WatchCallBack watchCallBack = new WatchCallBack(zooKeeper);
            watchCallBack.aWait(ZKConstant.ROOT_PATH);


            System.out.println("第二次读取本地缓存："+GoogleGuava.localCache.get(ZKConstant.CACHE_MAP_USER_KEY));



        }catch (Exception e){
            log.error("创建zookeeper客户端失败:" + e.getMessage());
        }
        return zooKeeper;
    }


}

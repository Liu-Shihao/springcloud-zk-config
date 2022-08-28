package com.lsh.auth.config;

import com.alibaba.fastjson.JSONObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.lsh.auth.entity.User;
import com.lsh.auth.repository.UserRepository;
import com.lsh.auth.watch.DefaultWatch;
import com.lsh.auth.watch.WatchCallBack;
import com.lsh.constant.ZKConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
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
    HazelcastInstance hazelcastClient;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ZookeeperProperties zookeeperProperties;



    @Bean("zkClient")
    public ZooKeeper zooKeeper() throws Exception {
        ZooKeeper zooKeeper = null;
//        try {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            zooKeeper = new ZooKeeper(zookeeperProperties.getAddress(),zookeeperProperties.getSessionTimeOut() , new DefaultWatch(countDownLatch,zooKeeper));
            countDownLatch.await();//等待zk客户端连接


            log.info("==========Zookeeper连接成功!==========");
            //查询db
            List<User> users = userRepository.findAll();

            //将数据写入hazelcast    hazelcast-user
            IMap<String, Object> cache = hazelcastClient.getMap(ZKConstant.CACHE_MAP_USER);
            // CACHE_MAP_USER_KEY ： users
            cache.put(ZKConstant.CACHE_MAP_USER_KEY, JSONObject.toJSONString(users));

            //zk维护version
            WatchCallBack watchCallBack  = new WatchCallBack(zooKeeper);
            watchCallBack.aWait(ZKConstant.ROOT_PATH);
//        }catch (Exception e){
//            log.error("创建zookeeper客户端失败:" + e.getMessage());
//        }
        return zooKeeper;
    }


}

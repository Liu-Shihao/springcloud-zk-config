package com.lsh.auth.config;


import com.hazelcast.core.HazelcastInstance;
import com.lsh.auth.watch.ZookeeperWatches;
import com.lsh.constant.ZKConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
public class ZookeeperWatchesConfig {

    @Autowired
    CuratorFramework curatorClient;

    @Autowired
    HazelcastInstance hazelcastInstance;

    @Bean
    public ZookeeperWatches zookeeperWatches(){
        ZookeeperWatches zookeeperWatches = new ZookeeperWatches(curatorClient,hazelcastInstance);
        //register listener
        List<String> nodes = Arrays.asList(ZKConstant.ZK_USER_PATH, ZKConstant.ZK_ROLE_PATH, ZKConstant.ZK_GROUP_PATH, ZKConstant.ZK_API_PATH);
        for (String path : nodes) {
            try {
                zookeeperWatches.znodeWatcher(path);
                zookeeperWatches.znodeChildrenWatcher(path);
            }catch (Exception e){
                log.error(e.getMessage());
            }
        }
        //recursion policy children node watcher
        zookeeperWatches.policyChildrenWatcher(ZKConstant.ZK_POLICY_PATH, zookeeperWatches);

        return zookeeperWatches;
    }



}
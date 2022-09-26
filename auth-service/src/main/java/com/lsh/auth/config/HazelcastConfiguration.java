package com.lsh.auth.config;

import com.alibaba.fastjson.JSONObject;
import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.lsh.auth.dto.zk.GroupNode;
import com.lsh.auth.dto.zk.RoleNode;
import com.lsh.auth.dto.zk.UserNode;
import com.lsh.constant.ZKConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@Configuration
public class HazelcastConfiguration {

    @Autowired
    CuratorFramework curatorClient;

    @Bean
    public Config hazelCastConfig() {
        Config config = new Config();
        //解决同网段下，不同库项目
        GroupConfig gc=new GroupConfig("hazelGroup");
        config.setInstanceName("hazelcast-instance")
                .addMapConfig(new MapConfig()
                        .setName("configuration")
                        // Map中存储条目的最大值[0~Integer.MAX_VALUE]。默认值为0。
                        .setMaxSizeConfig(new MaxSizeConfig(200, MaxSizeConfig.MaxSizePolicy.FREE_HEAP_SIZE))
                        //数据释放策略[NONE|LRU|LFU]。这是Map作为缓存的一个参数，用于指定数据的回收算法。默认为NONE。LRU：“最近最少使用“策略。
                        .setEvictionPolicy(EvictionPolicy.LRU)
                        //数据留存时间[0~Integer.MAX_VALUE]。缓存相关参数，单位秒，默认为0。
                        .setTimeToLiveSeconds(-1))
                .setGroupConfig(gc);
        return config;
    }

    /**
     * 添加Hazelcast监听器配置
     * @param config
     * @return
     */
    @Bean
    public HazelcastInstance hazelcastInstance(Config config) throws Exception {
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        log.info("load com.hazelcast.core.HazelcastInstance.....");
        log.info("init local cache .....");
        buildLocalCache(curatorClient,hazelcastInstance);
        return hazelcastInstance;
    }

    // build local cache
    public void buildLocalCache(CuratorFramework client,HazelcastInstance hazelcastInstance) throws Exception{
        List<String> users = client.getChildren().forPath(ZKConstant.ZK_USER_PATH);
        IMap<String, Object> localCache = hazelcastInstance.getMap("hazelcast-cache");
        for (String user : users) {
            byte[] bytes = client.getData().forPath(ZKConstant.ZK_USER_PATH + "/" + user);
            UserNode userNode = JSONObject.parseObject(new String(bytes), UserNode.class);
            for (String policy : userNode.getPolicys()) {
                localCache.put(ZKConstant.ZK_POLICY_PATH +policy,localCache.get(ZKConstant.ZK_POLICY_PATH +policy)+","+user);
            }
            for (String group : userNode.getGroups()) {
                byte[] bytes1 = client.getData().forPath(ZKConstant.ZK_GROUP_PATH + group);
                GroupNode groupNode = JSONObject.parseObject(new String(bytes1), GroupNode.class);
                for (String policy : groupNode.getPolicys()) {
//                    if (!localCache.get(ZKConstant.ZK_POLICY_PATH  +policy).toString().contains(user)){
                    localCache.put(ZKConstant.ZK_POLICY_PATH  +policy,localCache.get(ZKConstant.ZK_POLICY_PATH +policy)+","+user);
//                    }
                }
            }
            for (String role : userNode.getRoles()) {
                byte[] bytes2 = client.getData().forPath(ZKConstant.ZK_ROLE_PATH + role);
                RoleNode roleNode = JSONObject.parseObject(new String(bytes2), RoleNode.class);
                for (String policy : roleNode.getPolicys()) {
//                    if (!localCache.get(ZKConstant.ZK_POLICY_PATH  +policy).toString().contains(user)){
                    localCache.put(ZKConstant.ZK_POLICY_PATH  +policy,localCache.get(ZKConstant.ZK_POLICY_PATH  +policy)+","+user);
//                    }
                }
            }
        }
        List<String> groups = client.getChildren().forPath(ZKConstant.ZK_GROUP_PATH);
        for (String group : groups) {
            byte[] bytes = client.getData().forPath(ZKConstant.ZK_GROUP_PATH + "/" + group);
            GroupNode groupNode = JSONObject.parseObject(new String(bytes), GroupNode.class);
            List<String> group_users = groupNode.getUsers();
            localCache.put(ZKConstant.ZK_GROUP_PATH + "/" +group,group_users);
        }

        List<String> roles = client.getChildren().forPath(ZKConstant.ZK_ROLE_PATH);
        for (String role : roles) {
            byte[] bytes = client.getData().forPath(ZKConstant.ZK_ROLE_PATH + "/" + role);
            RoleNode roleNode = JSONObject.parseObject(new String(bytes), RoleNode.class);
            List<String> role_users = roleNode.getUsers();
            localCache.put(ZKConstant.ZK_ROLE_PATH + "/" +role,role_users);
        }
    }



}

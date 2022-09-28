package com.citi.zk.config;

import com.alibaba.fastjson.JSONObject;
import com.citi.zk.utils.BuildLocalCacheUtil;
import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.citi.zk.dto.zk.GroupNode;
import com.citi.zk.dto.zk.PolicyNode;
import com.citi.zk.dto.zk.RoleNode;
import com.citi.zk.dto.zk.UserNode;
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
    @Bean("hazelcastInstance")
    public HazelcastInstance hazelcastInstance(Config config) throws Exception {
        HazelcastInstance hazelcast = Hazelcast.newHazelcastInstance(config);
        log.info("load com.hazelcast.core.HazelcastInstance.....");
//        log.info("init local cache .....");
        //build local Cache
//        buildLocalCache(curatorClient,hazelcast);
        return hazelcast;
    }

    /**
     * build local cache
     * user : apis/roles/groups
     * role : users/policys
     * group : users/policys
     * policy : roles/groups
     * @param client
     * @throws Exception
     */
    public void buildLocalCache(CuratorFramework client,HazelcastInstance hazelcast) throws Exception{
        BuildLocalCacheUtil buildLocalCacheUtil = new BuildLocalCacheUtil(hazelcast,client);
        List<String> users = client.getChildren().forPath(ZKConstant.ZK_USER_PATH);
        for (String user : users) {
            byte[] bytes = client.getData().forPath(ZKConstant.ZK_USER_PATH + "/" + user);
            UserNode userNode = JSONObject.parseObject(new String(bytes), UserNode.class);
            // build User Cache，user ： api、role、group
            buildLocalCacheUtil.buildUserCache(userNode);
        }

        List<String> roles = client.getChildren().forPath(ZKConstant.ZK_ROLE_PATH);
        for (String role : roles) {
            byte[] bytes = client.getData().forPath(ZKConstant.ZK_ROLE_PATH + "/" + role);
            RoleNode roleNode = JSONObject.parseObject(new String(bytes), RoleNode.class);
            // build Role Cache ,role:users/policy
            buildLocalCacheUtil.buildRoleCache(roleNode);
        }
        List<String> groups = client.getChildren().forPath(ZKConstant.ZK_GROUP_PATH);
        for (String group : groups) {
            byte[] bytes = client.getData().forPath(ZKConstant.ZK_GROUP_PATH + "/" + group);
            GroupNode groupNode = JSONObject.parseObject(new String(bytes), GroupNode.class);
            // build Group Cache ,group:users/policy
            buildLocalCacheUtil.buildGroupCache(groupNode);
        }

        List<String> policys = client.getChildren().forPath(ZKConstant.ZK_POLICY_PATH);
        for (String policy : policys) {
            byte[] bytes = client.getData().forPath(ZKConstant.ZK_POLICY_PATH + "/" + policy);
            PolicyNode policyNode = JSONObject.parseObject(new String(bytes), PolicyNode.class);
            // build Group Cache ,policy:role/group
            buildLocalCacheUtil.buildPolicyCache(policyNode);
        }
    }

}

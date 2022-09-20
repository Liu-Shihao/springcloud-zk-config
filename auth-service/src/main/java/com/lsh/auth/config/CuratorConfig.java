package com.lsh.auth.config;

import com.alibaba.fastjson.JSONObject;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.lsh.auth.dto.zk.GroupNode;
import com.lsh.auth.dto.zk.PolicyNode;
import com.lsh.auth.dto.zk.RoleNode;
import com.lsh.auth.dto.zk.UserNode;
import com.lsh.auth.watch.ZookeeperWatches;
import com.lsh.constant.ZKConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/15 10:49
 * @Desc:  zk client ，building local cache
 */
@Slf4j
@Component
@Configuration
public class CuratorConfig {

    @Autowired
    Config hazelCastConfig;

    HazelcastInstance hzInstance = Hazelcast.newHazelcastInstance(hazelCastConfig);

    IMap<String, Object> localCache = hzInstance.getMap("hazelcast-cache");

    @Value("${zookeeper.address}")
    public String address ;

    @Bean("curatorClient")
    public CuratorFramework curatorClient() throws Exception {
        // baseSleepTimeMs:初始的sleep时间，用于计算之后的每次重试的sleep时间 ; maxRetries: 最大重试次数
        RetryPolicy retryPolicy  = new ExponentialBackoffRetry(1000,3);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                //连接地址  集群用,隔开
                .connectString(address)
                .connectionTimeoutMs(5000) //连接超时时间
                .sessionTimeoutMs(3000)//会话超时时间
                .retryPolicy(retryPolicy)//设置重试机制
                .build();
        client.start();
        //注册监听器
        ZookeeperWatches watches = new ZookeeperWatches(client);
        watches.znodeWatcher();
        watches.znodeChildrenWatcher();
        // build user permission Cache
        List<String> allUser = client.getChildren().forPath(ZKConstant.ZK_USER_PATH);
        for (String userPath : allUser) {
            byte[] bytes1 = client.getData().forPath(ZKConstant.ZK_USER_PATH+"/" + userPath);
            UserNode userNode = JSONObject.parseObject(new String(bytes1), UserNode.class);
            ArrayList<String> userGroups = userNode.getGroups();
            ArrayList<String> policys = new ArrayList<>();
            for (String userGroup : userGroups) {
                byte[] bytes2 = client.getData().forPath(ZKConstant.ZK_GROUP_PATH+userGroup);
                GroupNode groupNode = JSONObject.parseObject(new String(bytes2), GroupNode.class);
                policys.addAll(groupNode.getPolicys());
            }

            ArrayList<String> userRoles = userNode.getRoles();
            for (String userRole : userRoles) {
                byte[] bytes3 = client.getData().forPath(ZKConstant.ZK_ROLE_PATH+ userRole);
                RoleNode roleNode = JSONObject.parseObject(new String(bytes3), RoleNode.class);
                policys.addAll(roleNode.getPolicys());
            }

            HashSet<String> apis = new HashSet<>();
            for (String policy : policys) {
                byte[] bytes4 = client.getData().forPath(ZKConstant.ZK_POLICY_PATH+policy);
                PolicyNode policyNode = JSONObject.parseObject(new String(bytes4), PolicyNode.class);
                apis.addAll(policyNode.getApis());
            }
            log.info("cache user -> apis :  {} = {}",userPath,apis);
            localCache.put(userPath,apis);
        }
        return client;
    }




}

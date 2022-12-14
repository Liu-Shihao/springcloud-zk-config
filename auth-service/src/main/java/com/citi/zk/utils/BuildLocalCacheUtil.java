package com.citi.zk.utils;

import com.alibaba.fastjson.JSONObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.citi.zk.dto.zk.GroupNode;
import com.citi.zk.dto.zk.PolicyNode;
import com.citi.zk.dto.zk.RoleNode;
import com.citi.zk.dto.zk.UserNode;
import com.lsh.constant.ZKConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Slf4j
public class BuildLocalCacheUtil {

    HazelcastInstance hazelcastInstance;

    CuratorFramework curatorClient;

    public BuildLocalCacheUtil(@Qualifier("hazelcastInstance") HazelcastInstance hazelcastInstance, CuratorFramework curatorClient) {
        this.hazelcastInstance = hazelcastInstance;
        this.curatorClient = curatorClient;
    }

    /**
     * @param groupNode
     */
    public void buildGroupCache(GroupNode groupNode) {
        IMap<Object, Object> localCache = hazelcastInstance.getMap(ZKConstant.HAZELCAST_MAP);
        HashMap<String, Object> cacheValue = new HashMap<>();
        if (groupNode.getUsers() != null && !groupNode.getUsers().isEmpty()){
            for (String user : groupNode.getUsers()) {
                try {
                    // if group is not change，but it policy was changed ，rebuild Cache
                    curatorClient.setData().forPath(ZKConstant.ZK_USER_PATH + user,curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH + user));
                }catch (Exception e){
                    log.error(e.getMessage());
                }
            }
            cacheValue.put("users",groupNode.getUsers());
        }
        if (groupNode.getPolicys() != null && !groupNode.getPolicys().isEmpty()){
            cacheValue.put("groups",groupNode.getPolicys());
        }
//        log.info("{} =====> {}",ZKConstant.ZK_GROUP_PATH+groupNode.getPath(),cacheValue);
//        localCache.put(ZKConstant.ZK_GROUP_PATH+groupNode.getPath(),cacheValue);
    }

    public void buildRoleCache(RoleNode roleNode){
        IMap<Object, Object> localCache = hazelcastInstance.getMap(ZKConstant.HAZELCAST_MAP);
        HashMap<String, Object> cacheValue = new HashMap<>();
        if (roleNode.getUsers() != null && !roleNode.getUsers().isEmpty()){
            for (String user : roleNode.getUsers()) {
                try {
                    curatorClient.setData().forPath(ZKConstant.ZK_USER_PATH + user,curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH + user));
                }catch (Exception e){
                    log.error(e.getMessage());
                }
            }
            cacheValue.put("users",roleNode.getUsers());
        }
        if (roleNode.getPolicys() != null && !roleNode.getPolicys().isEmpty()){
            cacheValue.put("policys",roleNode.getPolicys());
        }
//        log.info("{} =====> {}",ZKConstant.ZK_ROLE_PATH+roleNode.getPath(),cacheValue);
//        localCache.put(ZKConstant.ZK_ROLE_PATH+roleNode.getPath(),cacheValue);
    }

    public void buildPolicyCache(PolicyNode policyNode) {
        IMap<Object, Object> localCache = hazelcastInstance.getMap(ZKConstant.HAZELCAST_MAP);
        HashMap<String, Object> cacheValue = new HashMap<>();
        if (policyNode.getGroups()!=null && !policyNode.getGroups().isEmpty()){
            for (String group : policyNode.getGroups()) {
                try {
                    curatorClient.setData().forPath(ZKConstant.ZK_GROUP_PATH + group,curatorClient.getData().forPath(ZKConstant.ZK_GROUP_PATH + group));
                }catch (Exception e){
                    log.error(e.getMessage());
                }
            }
            cacheValue.put("groups",policyNode.getGroups());
        }
        if (policyNode.getRoles()!=null && !policyNode.getRoles().isEmpty()){
            for (String role : policyNode.getRoles()) {
                try {
                    curatorClient.setData().forPath(ZKConstant.ZK_ROLE_PATH + role,curatorClient.getData().forPath(ZKConstant.ZK_ROLE_PATH + role));
                }catch (Exception e){
                    log.error(e.getMessage());
                }
            }
            cacheValue.put("roles",policyNode.getRoles());
        }

        // policy node changed ,rebuild user/role/group cache
        if (policyNode.getUsers()!=null && !policyNode.getUsers().isEmpty()){
            for (String user : policyNode.getUsers()) {
                try {
                    curatorClient.setData().forPath(ZKConstant.ZK_USER_PATH + user,curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH + user));
                }catch (Exception e){
                    log.error(e.getMessage());
                }
            }
            cacheValue.put("users",policyNode.getUsers());
        }
//        log.info("{} =====> {}",ZKConstant.ZK_POLICY_PATH+policyNode.getPath(),cacheValue);
//        localCache.put(ZKConstant.ZK_POLICY_PATH+policyNode.getPath(),cacheValue);
    }


    /**
     * builder user local cache
     * user : apis/roles/groups
     * @param userNode
     * @throws Exception
     */
    public void buildUserCache(UserNode userNode) {
        IMap<Object, Object> localCache = hazelcastInstance.getMap(ZKConstant.HAZELCAST_MAP);
        HashMap<String, Object> cacheValue = new HashMap<>();
        cacheValue.put("roles",userNode.getRoles());
        cacheValue.put("groups",userNode.getGroups());
        HashSet<String> policys = new HashSet<>();
        if (userNode.getPolicys()!= null && !userNode.getPolicys().isEmpty()){
            policys.addAll(userNode.getPolicys());
        }
        for (String group : userNode.getGroups()) {
            try {
                byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_GROUP_PATH + group);
                GroupNode groupNode = JSONObject.parseObject(new String(bytes), GroupNode.class);
                if (groupNode.getPolicys()!= null && !groupNode.getPolicys().isEmpty()){
                    policys.addAll(groupNode.getPolicys());
                }
            }catch (Exception e){
                log.error(e.getMessage());
            }
        }
        for (String role : userNode.getRoles()) {
            try {
                byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_ROLE_PATH + role);
                RoleNode roleNode = JSONObject.parseObject(new String(bytes), RoleNode.class);
                if (roleNode.getPolicys()!= null && !roleNode.getPolicys().isEmpty()){
                    policys.addAll(roleNode.getPolicys());
                }
            }catch (Exception e){
                log.error(e.getMessage());
            }
        }
        HashSet<String> apis = new HashSet<>();
        for (String policy : policys) {
            // find policy permission
            findPolicyPermission(ZKConstant.ZK_POLICY_PATH+policy,apis);
        }
        //build current user local cache
        cacheValue.put("apis",apis);
        log.info("cache :{} --> {}",ZKConstant.ZK_USER_PATH+userNode.getPath(),cacheValue);
        localCache.put(ZKConstant.ZK_USER_PATH+userNode.getPath(),cacheValue);
    }


    /**
     * find current node all permission
     * @param path
     * @param ans
     */
    public void findPolicyPermission(String path ,HashSet<String> ans) {
        try {
            byte[] bytes = curatorClient.getData().forPath(path);
            PolicyNode policyNode = JSONObject.parseObject(new String(bytes), PolicyNode.class);
            ans.addAll(policyNode.getApis());
            List<String> childrenPolicys = curatorClient.getChildren().forPath(path);
            if (childrenPolicys == null || childrenPolicys.size() == 0){
                return;
            }
            for (String childrenPolicy : childrenPolicys) {
                findPolicyPermission(path+"/"+childrenPolicy,ans);
            }
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }


}

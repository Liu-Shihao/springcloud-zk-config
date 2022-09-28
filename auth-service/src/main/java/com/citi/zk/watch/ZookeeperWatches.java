package com.citi.zk.watch;

import com.alibaba.fastjson.JSONObject;
import com.citi.zk.utils.BuildLocalCacheUtil;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.citi.zk.dto.zk.GroupNode;
import com.citi.zk.dto.zk.PolicyNode;
import com.citi.zk.dto.zk.RoleNode;
import com.citi.zk.dto.zk.UserNode;
import com.lsh.constant.ZKConstant;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.zookeeper.data.Stat;

import java.util.List;

@Data
@Slf4j
public class ZookeeperWatches {

    HazelcastInstance hazelcastInstance;

    private CuratorFramework client;

    BuildLocalCacheUtil buildLocalCacheUtil ;

    public ZookeeperWatches(CuratorFramework client,HazelcastInstance hazelcastInstance) {
        this.client = client;
        this.hazelcastInstance = hazelcastInstance;
        this.buildLocalCacheUtil = new BuildLocalCacheUtil(hazelcastInstance,client);
    }

    public void znodeWatcher(String path) throws Exception {
        NodeCache nodeCache = new NodeCache(client, path);
        nodeCache.start();
        nodeCache.getListenable().addListener(new NodeCacheListener() {

            @Override
            public void nodeChanged() throws Exception {
                log.info("=======Node Changed===========");
                String currentData = new String(nodeCache.getCurrentData().getData());
                nodeCache.getCurrentData().getStat();
                log.info("path:"+path);
                log.info("currentData:"+currentData);
            }
        });
        log.info("node monitor registration completed");
    }

    public void znodeChildrenWatcher(String path) throws Exception {
        String lock = "lock";
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, path,true);
        pathChildrenCache.start();
        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {

            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                synchronized (lock){
                    PathChildrenCacheEvent.Type type = event.getType();
                    String childrenData = new String(event.getData().getData());
                    String childrenPath = event.getData().getPath();
                    Stat childrenStat = event.getData().getStat();
                    log.info("event type："+type);
                    log.info("children path："+childrenPath);
                    log.info("children data："+childrenData);
                    log.info("children mate data："+childrenStat);

                    if (childrenPath.contains(ZKConstant.ZK_USER_PATH)){
                        log.info("node type : User");
                        //handle user node event
                        handleUserEvent(type.toString(),childrenPath,childrenData);
                    }else if (childrenPath.contains(ZKConstant.ZK_ROLE_PATH)){
                        log.info("node type : ROLE");
                        //handle role node event
                        handleRoleEvent(type.toString(),childrenPath,childrenData);
                    }else if (childrenPath.contains(ZKConstant.ZK_GROUP_PATH)){
                        log.info("node type : GROUP");
                        //handle group node event
                        handleGroupEvent(type.toString(),childrenPath,childrenData);
                    }else if (childrenPath.contains(ZKConstant.ZK_POLICY_PATH)){
                        log.info("node type : POLICY");
                        //handle policy node event
                        handlePolicyEvent(type.toString(),childrenPath,childrenData);
                    }else if (childrenPath.contains(ZKConstant.ZK_API_PATH)){
                        log.info("node type : API");
                    }
                }
            }
        });
        log.info("children node monitor registration is complete");
    }

    public void handleGroupEvent(String eventType, String groupPath, String childrenData) {
        GroupNode groupNode = JSONObject.parseObject(childrenData, GroupNode.class);
        switch (eventType){
            case "CHILD_ADDED":
                //build group cache
                buildLocalCacheUtil.buildGroupCache(groupNode);
                break;
            case "CHILD_UPDATED":
                //rebuild group cache
                buildLocalCacheUtil.buildGroupCache(groupNode);
                break;
            case "CHILD_REMOVED":
                IMap<Object, Object> localCache = hazelcastInstance.getMap(ZKConstant.HAZELCAST_MAP);
                localCache.remove(groupPath);
                break;
            default:
                break;
        }

    }


    /**
     *
     * @param eventType
     * @param rolePath
     * @param childrenData
     */
    public void handleRoleEvent(String eventType, String rolePath, String childrenData) {
        RoleNode roleNode = JSONObject.parseObject(childrenData, RoleNode.class);
        switch (eventType){
            case "CHILD_ADDED":
                //build role cache
                buildLocalCacheUtil.buildRoleCache(roleNode);
                break;
            case "CHILD_UPDATED":
                //rebuild role cache
                buildLocalCacheUtil.buildRoleCache(roleNode);
                break;
            case "CHILD_REMOVED":
                IMap<Object, Object> localCache = hazelcastInstance.getMap(ZKConstant.HAZELCAST_MAP);
                localCache.remove(rolePath);
                break;
            default:
                break;
        }

    }



    /**
     * Handle the logic of updating the cache according to the different event types of the User Node
     */
    public void handleUserEvent(String eventType,String childrenPath,String childrenData){
        UserNode userNode = JSONObject.parseObject(childrenData, UserNode.class);
        switch (eventType){
            case "CHILD_ADDED":
                // add user node: build user cache
                buildLocalCacheUtil.buildUserCache(userNode);
                break;
            case "CHILD_UPDATED":
                //user node update:rebuild user cache
                buildLocalCacheUtil.buildUserCache(userNode);
                break;
            case "CHILD_REMOVED":
                //delete user node:remove user cache
                IMap<Object, Object> localCache = hazelcastInstance.getMap(ZKConstant.HAZELCAST_MAP);
                localCache.remove(childrenPath);
                break;
            default:
                break;
        }
    }

    /**
     *
     * @param eventType
     * @param childrenPath
     * @param childrenData
     */
    public void handlePolicyEvent(String eventType,String childrenPath,String childrenData){
        PolicyNode policyNode = JSONObject.parseObject(childrenData, PolicyNode.class);
        switch (eventType){
            case "CHILD_ADDED":
                //build policy cache
                buildLocalCacheUtil.buildPolicyCache(policyNode);
                break;
            case "CHILD_UPDATED":
                //rebuild policy cache
                buildLocalCacheUtil.buildPolicyCache(policyNode);
                break;
            case "CHILD_REMOVED":
                IMap<Object, Object> localCache = hazelcastInstance.getMap(ZKConstant.HAZELCAST_MAP);
                localCache.remove(childrenPath);
                break;
            default:
                break;
        }
    }

    /**
     * recursion policy children node watcher
     * @param path
     * @param watches
     * @throws Exception
     */
    public void policyChildrenWatcher(String path,ZookeeperWatches watches) {
        try {
            List<String> childrenPolicys = client.getChildren().forPath(path);
            if (childrenPolicys == null || childrenPolicys.size() == 0){
                return;
            }
            watches.znodeWatcher(path);
            watches.znodeChildrenWatcher(path);
            for (String childrenPolicy : childrenPolicys) {
                policyChildrenWatcher(path+"/"+childrenPolicy,watches);
            }
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }
}

package com.lsh.auth.watch;

import com.alibaba.fastjson.JSONObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.lsh.auth.dto.zk.GroupNode;
import com.lsh.auth.dto.zk.PolicyNode;
import com.lsh.auth.dto.zk.RoleNode;
import com.lsh.auth.dto.zk.UserNode;
import com.lsh.constant.ZKConstant;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Data
@Slf4j
public class ZookeeperWatches {

    HazelcastInstance hazelcastInstance;

    private CuratorFramework client;

    public ZookeeperWatches(CuratorFramework client,HazelcastInstance hazelcastInstance) {
        this.client = client;
        this.hazelcastInstance = hazelcastInstance;
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
        log.info("节点监听注册完成");
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
                    if (childrenPath.contains(ZKConstant.ZK_USER_PATH)){
                        log.info("======= user zk node was "+type+" =======");
                        //rebuilder user local cache
                        buildUserCache(childrenPath,childrenData);
                    }else if (childrenPath.contains(ZKConstant.ZK_ROLE_PATH)){
                        log.info("======= role zk node was "+type+" =======");
                    }else if (childrenPath.contains(ZKConstant.ZK_GROUP_PATH)){
                        log.info("======= group zk node was "+type+" =======");
                    }else if (childrenPath.contains(ZKConstant.ZK_POLICY_PATH)){
                        log.info("======= policy zk node was "+type+" =======");
                        PolicyNode policyNode = JSONObject.parseObject(childrenData, PolicyNode.class);
                        String[] split = childrenPath.split("/");
                        int level = split.length-1;
                        switch (type.toString()){
                            case "CHILD_ADDED":
                                log.info("Add policy node");
                                break;
                            case "CHILD_UPDATED":
                                log.info("Update policy node");

                                break;
                            case "CHILD_REMOVED":
                                log.info("Remove policy node");
                                break;
                            default:
                                break;
                        }

                    }else if (childrenPath.contains(ZKConstant.ZK_API_PATH)){
                        log.info("======= api zk node was "+type+" =======");
                    }
                    Stat childrenStat = event.getData().getStat();
                    log.info("event type："+type);
                    log.info("children path："+childrenPath);
                    log.info("children data："+childrenData);
                    log.info("children mate data："+childrenStat);
                }


            }
        });
        log.info("子节点监听注册完成");
    }


    /**
     * rebuilder user local cache
     * @param childrenPath
     * @param childrenData
     * @throws Exception
     */
    public void buildUserCache(String childrenPath,String childrenData) throws Exception{
        UserNode userNode = JSONObject.parseObject(childrenData, UserNode.class);

        ArrayList<String> userGroups = userNode.getGroups();
        HashSet<String> policys = new HashSet<>();
        policys.addAll(userNode.getPolicys());
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
            log.info("cache user -> apis :  {} = {}",userNode.getPath(),apis);

            IMap<String, Object> localCache = hazelcastInstance.getMap("hazelcast-cache");
            localCache.put(userNode.getPath(),apis);
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

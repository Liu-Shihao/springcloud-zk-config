package com.lsh.auth.watch;

import com.alibaba.fastjson.JSONObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.lsh.auth.dto.zk.GroupNode;
import com.lsh.auth.dto.zk.PolicyNode;
import com.lsh.auth.dto.zk.RoleNode;
import com.lsh.auth.dto.zk.UserNode;
import com.lsh.auth.utils.BeanUtils;
import com.lsh.constant.ZKConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Component
@Slf4j
public class ZookeeperWatches {

//    @Autowired
//    Config hazelCastConfig;
//
//    HazelcastInstance hzInstance = Hazelcast.newHazelcastInstance(hazelCastConfig);
//




    private CuratorFramework client;

    public ZookeeperWatches(CuratorFramework client) {
        this.client = client;
    }

    List<String> nodes = Arrays.asList("/users", "/roles", "/groups", "/policys", "/apis");


    public void znodeWatcher() throws Exception {

        for (String path : nodes) {
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

        }
        log.info("节点监听注册完成");
    }

    public void znodeChildrenWatcher() throws Exception {
        String lock = "lock";
        for (String path : nodes) {
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
                            buildUserCache(childrenPath,childrenData);
                        }else if (childrenPath.contains(ZKConstant.ZK_ROLE_PATH)){
                            log.info("======= role zk node was "+type+" =======");
                        }else if (childrenPath.contains(ZKConstant.ZK_GROUP_PATH)){
                            log.info("======= group zk node was "+type+" =======");
                        }else if (childrenPath.contains(ZKConstant.ZK_POLICY_PATH)){
                            log.info("======= policy zk node was "+type+" =======");
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
        }


        log.info("子节点监听注册完成");
    }

    public void buildUserCache(String childrenPath,String childrenData) throws Exception{
        // build user permission cache
//        List<String> allUser = client.getChildren().forPath(ZKConstant.ZK_USER_PATH);
//        for (String userPath : allUser) {
//            byte[] bytes1 = client.getData().forPath(userPath);
            UserNode userNode = JSONObject.parseObject(childrenData, UserNode.class);
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
//            }
            log.info("cache user -> apis :  {} = {}",userNode.getPath(),apis);

            HazelcastInstance hzInstance = BeanUtils.getBean(com.hazelcast.core.HazelcastInstance.class);
            IMap<String, Object> localCache = hzInstance.getMap("hazelcast-cache");
            localCache.put(userNode.getPath(),apis);
        }
    }
}

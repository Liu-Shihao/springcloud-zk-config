package com.lsh.auth.watch;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class ZookeeperWatches {

    @Autowired
    Config hazelCastConfig;

    HazelcastInstance hzInstance = Hazelcast.newHazelcastInstance(hazelCastConfig);

    IMap<String, Object> localCache = hzInstance.getMap("hazelcast-cache");

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
                    log.info("=======节点改变===========");
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
        for (String path : nodes) {
            PathChildrenCache pathChildrenCache = new PathChildrenCache(client, path,true);
            pathChildrenCache.start();
            pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {

                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    log.info("=======节点子节点改变===========");
                    PathChildrenCacheEvent.Type type = event.getType();
                    String childrenData = new String(event.getData().getData());
                    String childrenPath = event.getData().getPath();
                    Stat childrenStat = event.getData().getStat();

                    log.info("子节点监听类型："+type);
                    log.info("子节点路径："+childrenPath);
                    log.info("子节点数据："+childrenData);
                    log.info("子节点元数据："+childrenStat);

                }
            });
        }


        log.info("子节点监听注册完成");
    }
}

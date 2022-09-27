package com.lsh.auth.config;

import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/31 12:27
 * @Desc:
 */
@Slf4j
@Component
public class MyWatch implements Watcher {

    @Autowired
    LoadingCache<String, Set<String>> localCache;

    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case None:
                break;
            case NodeCreated:
                log.info("{} 节点被创建，清空本地缓存",event.getPath());
                localCache.invalidateAll();
//                localCache.invalidate();
                break;
            case NodeDeleted:
                log.info("{} 节点被删除，清空本地缓存",event.getPath());
                localCache.invalidateAll();
                break;
            case NodeDataChanged:
                log.info("{} 节点数据被修改，清空本地缓存",event.getPath());
                localCache.invalidateAll();
                break;
            case NodeChildrenChanged:
                log.info("{} 子节点改变，清空本地缓存",event.getPath());
                localCache.invalidateAll();
                break;
        }

    }
}
package com.lsh.auth.controller;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.lsh.constant.ZKConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: LiuShihao
 * @Date: 2022/9/21 17:46
 * @Desc:
 */
@Slf4j
@RestController
@RequestMapping("/cache")
public class CacheController {

    @Qualifier("hazelcastInstance")
    @Autowired
    HazelcastInstance hazelcastInstance;

    @GetMapping("/{path1}/{path2}")
    public Object getUserCache(@PathVariable("path1") String path1,@PathVariable("path2") String path2){
        IMap<String, Object> localCache = hazelcastInstance.getMap(ZKConstant.HAZELCAST_MAP);
        String path = "/"+path1+"/"+path2;
        log.info("cache key: {}",path);
        return localCache.get(path);
    }
}

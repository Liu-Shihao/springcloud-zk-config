package com.lsh.auth.controller;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.lsh.auth.utils.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: LiuShihao
 * @Date: 2022/9/21 17:46
 * @Desc:
 */
@RestController
@RequestMapping("/cache")
public class CacheController {



    @GetMapping("/{user}")
    public Object getUserCache(@PathVariable("user") String user){
        HazelcastInstance hazelcastInstance = BeanUtils.getBean(com.hazelcast.core.HazelcastInstance.class);
        IMap<String, Object> localCache = hazelcastInstance.getMap("hazelcast-cache");
        return localCache.get("/"+user);
    }
}

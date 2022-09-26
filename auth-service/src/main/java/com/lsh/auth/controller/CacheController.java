package com.lsh.auth.controller;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.lsh.constant.ZKConstant;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    HazelcastInstance hazelcastInstance;

    @GetMapping("/{user}")
    public Object getUserCache(@PathVariable("user") String user){
        IMap<String, Object> localCache = hazelcastInstance.getMap("hazelcast-cache");
        return localCache.get("/"+user);
    }


    @GetMapping("/{id}/{path}")
    public Object getCache(@PathVariable("id") Integer id,@PathVariable("path") String path){
        IMap<String, Object> localCache = hazelcastInstance.getMap("hazelcast-cache");
        switch (id){
            case 1:
                return  localCache.get(ZKConstant.ZK_GROUP_PATH + "/" +path);
            case 2:
                return  localCache.get(ZKConstant.ZK_ROLE_PATH + "/" +path);
            default:
                return  localCache.get(ZKConstant.ZK_POLICY_PATH + "/" +path);
        }

    }
}

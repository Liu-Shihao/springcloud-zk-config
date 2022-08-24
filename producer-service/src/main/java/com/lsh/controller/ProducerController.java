package com.lsh.controller;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/24 14:51
 * @Desc:
 */
@RestController
@RequestMapping("/ps")
public class ProducerController {

    @Autowired
    HazelcastInstance hazelcastInstance;

    @GetMapping("/test")
    public String test(){
        IMap<String, Object> map = hazelcastInstance.getMap("zk-cache");
        String data = (String) map.get("test");
        System.out.println("cache data:"+data);
        return data;
    }

    @GetMapping("/index")
    public String index(){
        System.out.println("Producer Service被调用");
        return "success";
    }
}

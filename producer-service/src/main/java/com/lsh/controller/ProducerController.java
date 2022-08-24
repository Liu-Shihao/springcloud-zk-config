package com.lsh.controller;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/24 14:51
 * @Desc:
 */
@RestController
@RequestMapping("/ps")
public class ProducerController {

    Random random = new Random();

    @Autowired
    HazelcastInstance hazelcastInstance;

    @GetMapping("/test")
    public String test(){
        int nextInt = random.nextInt(100);
        System.out.println("random num:"+nextInt);
        IMap<String, Object> map = hazelcastInstance.getMap("zk-cache");
        String data = (String) map.get("test");
        System.out.println("cache data:"+data);
        return data;
    }
}

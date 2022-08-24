package com.lsh.controller;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/24 14:51
 * @Desc:
 */
@RestController
@RequestMapping("/cs")
public class ConsumerController {

    @Autowired
    HazelcastInstance hazelcastInstance;

    @Autowired
    RestTemplate restTemplate;

    @GetMapping("/test")
    public String test(){
        IMap<String, Object> map = hazelcastInstance.getMap("zk-cache");
        String data = (String) map.get("test");
        System.out.println("cache data:"+data);
        return data;
    }

    @GetMapping("/call")
    public String callProducerService(String username){
        String url = "http://localhost:8002/ps/index";
        HttpHeaders headers = new HttpHeaders();
        headers.add("username",username);
        HttpEntity<Object> httpEntity = new HttpEntity<>(null,headers);
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
        return exchange.getBody();
    }
}

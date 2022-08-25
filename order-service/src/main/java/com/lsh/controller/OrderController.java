package com.lsh.controller;

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
@RequestMapping("/order")
public class OrderController {

    @Autowired
    RestTemplate restTemplate;

    @GetMapping("/test")
    public String test(){
        return "success";
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

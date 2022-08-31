package com.lsh.auth.controller;

import com.alibaba.fastjson.JSONObject;
import com.lsh.auth.dto.ZkNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/30 14:03
 * @Desc:
 */
@Slf4j
@RestController
@RequestMapping("/zk")
public class ZKController {

    @Autowired
    ZooKeeper zkClient;

    /**
     * EPHEMERAL 临时节点
     * PERSISTENT 永久节点
     * @param node
     * @return
     */
    @PostMapping("/createZkNode")
    public String createZkNode(@RequestBody ZkNode node) throws Exception{
        log.info("type :{}",node.getType());
        Object data;
        if (node.getTypes().contains(node.getType())){
            data = node.getArrays();
        }else {
            List<String> groups = node.getGroups();
            List<String> roles = node.getRoles();
            HashMap<String, Object> map = new HashMap<>();
            map.put("roles",roles);
            map.put("groups",groups);
            data = map;
        }
        String jsonString = JSONObject.toJSONString(data);
        log.info("data :{}",jsonString);
        zkClient.create(node.getPath(),jsonString.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        return  "success";
    }

    @PostMapping("/getData")
    public String getData(@RequestBody ZkNode node) throws Exception{
        log.info("getData Path :/{}",node.getPath());
        byte[] data = zkClient.getData(node.getPath(), true, new Stat());
        return new String(data);
    }
    @PostMapping("/setData")
    public String setData(@RequestBody ZkNode node) throws Exception{
        log.info("setData Path :/{}",node.getPath());
        log.info("type :{}",node.getType());
        Object data;
        if (node.getTypes().contains(node.getType())){
            data = node.getArrays();
        }else {
            List<String> groups = node.getGroups();
            List<String> roles = node.getRoles();
            HashMap<String, Object> map = new HashMap<>();
            map.put("roles",roles);
            map.put("groups",groups);
            data = map;
        }
        String jsonString = JSONObject.toJSONString(data);
        log.info("data :{}",jsonString);
        Stat stat = new Stat();
        zkClient.setData(node.getPath(),jsonString.getBytes(),stat.getVersion());
        return "success";
    }


}
package com.lsh.auth.controller;

import com.lsh.auth.dto.ZkNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: LiuShihao
 * @Date: 2022/9/8 10:36
 * @Desc:
 */

@Slf4j
@RestController
@RequestMapping("/curator")
public class CuratorController {

    @Autowired
    CuratorFramework curatorClient;

    public String createNode(@RequestBody ZkNode node) throws Exception {
        String path = node.getPath();
        //默认创建持久节点
        curatorClient.create().forPath(path,"".getBytes());
        //创建临时节点，并递归创建父节点
        curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);

        return null;
    }









}

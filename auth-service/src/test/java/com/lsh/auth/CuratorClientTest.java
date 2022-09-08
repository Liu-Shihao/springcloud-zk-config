package com.lsh.auth;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

/**
 * @Author: LiuShihao
 * @Date: 2022/9/8 11:01
 * @Desc:
 */
@SpringBootTest
public class CuratorClientTest {

    CuratorFramework curatorClient;

    @Test
    public void create() throws Exception {
        //默认创建持久节点
        curatorClient.create().forPath("/"+UUID.randomUUID(),"".getBytes());
        //创建临时节点，并递归创建父节点（父节点为持久节点）
        curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/creating-EPHEMERAL-ParentsIfNeeded"+UUID.randomUUID());
        //创建持久节点，并递归创建父节点
        curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/creating-PERSISTENT-ParentsIfNeeded/"+UUID.randomUUID());
    }


    @Test
    public void del() throws Exception {

        curatorClient.delete().forPath("/4294c8f3-1487-4880-a886-1d20a842a046");//删除一个子节点

        curatorClient.delete().deletingChildrenIfNeeded().forPath("/creatingParentsIfNeeded");   //删除节点并递归删除其子节点

        curatorClient.delete().withVersion(-1).forPath("/c7bcb041-5ebc-4382-b5e2-cd12c2a8d4aa");//指定版本进行删除,如果此版本已经不存在，则删除异常

        curatorClient.delete().guaranteed().forPath("/369983f9-7d52-48d6-b0db-6a673af4b67a");//强制删除

    }


    @Test
    public void getData() throws Exception {
        String path = "/TestGetData";
        curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path,UUID.randomUUID().toString().getBytes());
        byte[] bytes = curatorClient.getData().forPath(path);
        System.out.println(new String(bytes));

    }
    @Test
    public void setData() throws Exception {
        String path = "/TestGetData";
        curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path,UUID.randomUUID().toString().getBytes());
        byte[] bytes1 = curatorClient.getData().forPath(path);

        System.out.println("before update :"+new String(bytes1));
        curatorClient.setData().forPath(path,UUID.randomUUID().toString().getBytes());
        byte[] bytes2 = curatorClient.getData().forPath(path);

        System.out.println("after update :"+new String(bytes2));
    }

    @Test
    public void async() throws Exception {
        String path ="/"+UUID.randomUUID();
        curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path,UUID.randomUUID().toString().getBytes());

        //inBackground 异步
        curatorClient.getData().inBackground((client, event) -> {
            System.out.println();
            System.out.println("当前线程：" + Thread.currentThread().getName() + ",code:" + event.getResultCode() + ",type:" + event.getType()+",data:"+new String(event.getData()));
        }).forPath(path);
        System.in.read();
    }




        @Before
    public void conn(){
        RetryPolicy retryPolicy  = new ExponentialBackoffRetry(1000,3);
        curatorClient = CuratorFrameworkFactory.builder()
                //连接地址  集群用,隔开
                .connectString("192.168.153.131:2181")
                .connectionTimeoutMs(5000) //连接超时时间
                .sessionTimeoutMs(3000)//会话超时时间
                .retryPolicy(retryPolicy)//设置重试机制
                .build();
        curatorClient.start();
    }


    @After
    public void done(){
        System.out.println("done");
    }
}

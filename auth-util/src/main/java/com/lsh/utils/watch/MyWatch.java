package com.lsh.utils.watch;

import com.lsh.constant.ZKConstant;
import com.lsh.utils.config.GoogleGuava;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/29 16:08
 * @Desc:
 */
public class MyWatch implements Watcher, AsyncCallback.ChildrenCallback.ChildrenCallback, AsyncCallback.StatCallback {

    ZooKeeper zooKeeper;

    CountDownLatch countDownLatch = new CountDownLatch(1);

    public MyWatch(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    //将zk数据加载到本地缓存
    public void loadingLocalCache()  {
        try {
            System.out.println("第一次查询本地缓存：user1:"+GoogleGuava.localCache.get("user1"));
            zooKeeper.exists(ZKConstant.ZK_USER_PATH, this,this,null);
            countDownLatch.await();
            zooKeeper.getChildren(ZKConstant.ZK_GROUP_PATH, this);
            zooKeeper.getChildren(ZKConstant.ZK_ROLE_PATH, this);
            zooKeeper.getChildren(ZKConstant.ZK_POLICY_PATH, this);
            zooKeeper.getChildren(ZKConstant.ZK_API_PATH, this);
            List<String> users = zooKeeper.getChildren(ZKConstant.ZK_USER_PATH, this);
            for (String user : users) {
                //获得每个用户的group
//                await(countDownLatch,ZKConstant.ZK_USER_PATH+"/" + user + ZKConstant.ZK_USER_GROUP_PATH);
                List<String> groups = zooKeeper.getChildren(ZKConstant.ZK_USER_PATH+"/" + user + ZKConstant.ZK_USER_GROUP_PATH, this);
                ArrayList<String> policys = new ArrayList<>();
                for (String group : groups) {
                    //获得该用户该分组的策略
//                    await(countDownLatch,ZKConstant.ZK_GROUP_PATH+"/" + group);
                    List<String> groupPolicy = zooKeeper.getChildren(ZKConstant.ZK_GROUP_PATH+"/" + group,  this);
                    policys.addAll(groupPolicy);
                }
                //获得每个用户的role
//                await(countDownLatch,ZKConstant.ZK_USER_PATH+"/" + user + ZKConstant.ZK_USER_ROLE_PATH);
                List<String> roles = zooKeeper.getChildren(ZKConstant.ZK_USER_PATH+"/" + user + ZKConstant.ZK_USER_ROLE_PATH, this);
                for (String role : roles) {
                    //获得该用户该角色的策略
//                    await(countDownLatch,ZKConstant.ZK_ROLE_PATH+"/" + role);
                    List<String> rolePolicy = zooKeeper.getChildren(ZKConstant.ZK_ROLE_PATH+"/" + role, this);
                    policys.addAll(rolePolicy);
                }
                HashSet<String> apis = new HashSet<>();

                //获得所有策略的apis
                for (String policy : policys) {
//                    await(countDownLatch,ZKConstant.ZK_POLICY_PATH+"/" + policy);
                    List<String> api = zooKeeper.getChildren(ZKConstant.ZK_POLICY_PATH+"/" + policy, this);
                    apis.addAll(api);
                }
                System.out.println(user+" :"+apis.toString());
                // 3.将权限数据写入本地缓存
                GoogleGuava.localCache.put(user,apis);
            }
            System.out.println("第二次查询本地缓存：user1:"+GoogleGuava.localCache.get("user1"));
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void await(CountDownLatch countDownLatch,String key) throws Exception {
        countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
        zooKeeper.exists(key, this,this,null);
    }



    @Override
    public void process(WatchedEvent event)  {
        try {
            switch (event.getType()) {
                case None:
                    break;
                case NodeCreated:
                    //节点创建
                    System.out.println("===="+event.getPath()+"节点创建====");
                    countDownLatch.countDown();
                    GoogleGuava.localCache.invalidateAll();
                    break;
                case NodeDeleted:
                    //节点被删除
                    System.out.println("===="+event.getPath()+"节点被删除====");
                    GoogleGuava.localCache.invalidateAll();
                    break;
                case NodeDataChanged:
                    //节点数据改变
                    System.out.println("===="+event.getPath()+"节点数据改变====");
                    GoogleGuava.localCache.invalidateAll();
                    break;
                case NodeChildrenChanged:
                    //子节点改变
                    System.out.println("===="+event.getPath()+"子节点改变====");
                    countDownLatch = new CountDownLatch(1);
                    GoogleGuava.localCache.invalidateAll();
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * exists 判断节点是否存在 异步回调
     */
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        if (stat != null){
            System.out.println("======="+path+" 节点存在=======");
            countDownLatch.countDown();
        }else {
            System.out.println(path+"不存在，等待被创建...");
        }
    }

    //
    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children) {


    }
}

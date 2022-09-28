package com.citi.zk.watch;

import lombok.Data;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.stereotype.Component;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/15 23:50
 * @Desc:  实现三个接口
 * Watcher 注册节点监控回调
 * StatCallback exists 节点是否存在异步回调
 * DataCallback getData 获取数据异步回调
 */
@Data
@Component
public class WatchCallBack implements Watcher, AsyncCallback.StatCallback, AsyncCallback.DataCallback {


    ZooKeeper zooKeeper;

    //当前类存在new出的对象，所以无法使用@Autowired注入

    public WatchCallBack(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    public WatchCallBack() {

    }

    public void aWait(String path)  {
        //判断节点是否存在，会出发exists异步回调方法（方法2）
        zooKeeper.exists(path, this, this,null);
    }


    /**
     * 2 exists 判断节点是否存在 异步回调
     */
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        System.out.println("=======exists 回调=======");
        try {
            if (stat != null){
                System.out.println("======="+path+" 节点存在=======");
                //触发getData的异步回调方法（方法1）
                zooKeeper.setData(path,"{version:1}".getBytes(),stat.getVersion());
            }else {
                System.out.println("======="+path+" 节点不存在,创建节点=======");
                zooKeeper.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 3 watch 观察节点 回调方法
     */
    @Override
    public void process(WatchedEvent event) {
//        System.out.println("WatchedEvent 回调："+event.getState());
        switch (event.getType()) {
            case None:
                break;
            case NodeCreated:
                System.out.println("=======WatchedEvent 回调：节点被创建=======");
                break;
            case NodeDeleted:
                System.out.println("=======WatchedEvent 回调：节点被删除=======");
                //并且countDownLatch重新赋值
//                countDownLatch = new CountDownLatch(1);
                break;
            case NodeDataChanged:
                System.out.println("=======WatchedEvent 回调：节点数据被更改=======");
                //注意：如果数据被变更，则需要重新获取数据
                break;
            case NodeChildrenChanged:
                break;
        }

    }

    @Override
    public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
        if (data != null){
            //如果数据不为空，则拉去缓存，countDown -1
            String str = new String(data);
            System.out.println("=======getData 回调："+str+"=======");
            //本地缓存
//            IMap<String, Object> zkCache = hazelcastInstance.getMap("zk-cache");
            //api : users
//            zkCache.put("test",str);
//            countDownLatch.countDown();
        }else {
            //数据为空，countDown继续阻塞，如果节点有数据写入，发生NodeDataChanged事件，触发watch回调方法（方法3）
            System.out.println("=======getData 回调 :没有数据=======");
        }
    }
}

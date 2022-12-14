package com.lsh.watch;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.lsh.utils.BeanUtils;
import lombok.Data;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

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

    HazelcastInstance hazelcastInstance = BeanUtils.getBean(HazelcastInstance.class);

    ZooKeeper zooKeeper;

    //当前类存在new出的对象，所以无法使用@Autowired注入
    CountDownLatch countDownLatch = new CountDownLatch(1);

    public WatchCallBack(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    public WatchCallBack() {

    }

    public void aWait(String path)  {
        //判断节点是否存在，会出发exists异步回调方法（方法2）
        zooKeeper.exists(path, this, this,null);
        try {
            countDownLatch.await();//等待拉去缓存
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        System.out.println("=======本地缓存拉去成功！=======");
    }


    /**
     * 1 getData 异步回调
     */
    @Override
    public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {

        if (data != null){
            //如果数据不为空，则拉去缓存，countDown -1
            String str = new String(data);
            System.out.println("=======getData 回调："+str+"=======");
            //本地缓存
            IMap<String, Object> zkCache = hazelcastInstance.getMap("zk-cache");
            //api : users
            zkCache.put("test",str);
            countDownLatch.countDown();
        }else {
            //数据为空，countDown继续阻塞，如果节点有数据写入，发生NodeDataChanged事件，触发watch回调方法（方法3）
            System.out.println("=======getData 回调 :没有数据=======");
        }
    }

    /**
     * 2 exists 判断节点是否存在 异步回调
     */
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        System.out.println("=======xists 回调=======");
        if (stat != null){
            System.out.println("======="+path+" 节点存在=======");
            //触发getData的异步回调方法（方法1）
            zooKeeper.getData(path,this,this,null);
        }else {
            System.out.println("======="+path+" 节点不存在=======");
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
                zooKeeper.getData(event.getPath(),this,this,null);
                break;
            case NodeDeleted:
                System.out.println("=======WatchedEvent 回调：节点被删除=======");
                //清空zk-cache
                IMap<String, Object> zkCache = hazelcastInstance.getMap("zk-cache");
                zkCache.clear();
                //并且countDownLatch重新赋值
                countDownLatch = new CountDownLatch(1);
                break;
            case NodeDataChanged:
                System.out.println("=======WatchedEvent 回调：节点数据被更改=======");
                //注意：如果数据被变更，则需要重新获取数据
                zooKeeper.getData(event.getPath(),this,this,null);
                break;
            case NodeChildrenChanged:
                break;
        }

    }

}

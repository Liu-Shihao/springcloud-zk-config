package com.lsh.watch;

import com.lsh.utils.MyConf;
import lombok.Data;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

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
public class WatchCallBack implements Watcher, AsyncCallback.StatCallback, AsyncCallback.DataCallback {

    //注意，getData数据时需要使用zk对象，需要手动set
    ZooKeeper zooKeeper;

    String path;

    MyConf myConf;

    CountDownLatch countDownLatch = new CountDownLatch(1);

    public WatchCallBack(ZooKeeper zooKeeper, String path) {
        this.zooKeeper = zooKeeper;
        this.path = path;
    }

    public void aWait()  {
        //判断节点是否存在，会出发exists异步回调方法（方法2）
        zooKeeper.exists(path, this, this,"aaa");
        try {
            countDownLatch.await();//等待MyConf数据取完
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }


    /**
     * 1 getData 异步回调
     */
    @Override
    public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {

        if (data != null){
            //如果数据不为空，则设置myConf，countDown -1
            System.out.println("getData 回调："+new String(data));
            myConf.setConf(new String(data));
            countDownLatch.countDown();
        }else {
            //数据为空，countDown继续阻塞，如果节点有数据写入，发生NodeDataChanged事件，触发watch回调方法（方法3）
            System.out.println("getData 回调 :没有数据");
        }

    }

    /**
     * 2 exists 判断节点是否存在 异步回调
     */
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        System.out.println("======exists 回调======");
        if (stat != null){
            System.out.println("======"+path+" 节点存在======");
            //触发getData的异步回调方法（方法1）
            zooKeeper.getData(path,this,this,"abc");
        }else {
            System.out.println("====== "+path+" 节点不存在======");
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
                System.out.println("WatchedEvent 回调：节点被创建");
                //节点刚被创建出来
                //整个流程：首先在TestConfig类中判断节点是否存在，此时节点还未创建，则被阻塞在aWait()方法，等到节点被创建出，发生watch回调，即此处，然后继续获得数据，出发getData回调方法，从而完成aWait方法（）
                zooKeeper.getData(event.getPath(),this,this,"abc");
                break;
            case NodeDeleted:
                System.out.println("WatchedEvent 回调：节点被删除");
                //容忍性问题
                //如果节点被删除，数据要怎么处理？
                //清空myConf
                myConf.setConf("");
                //并且countDownLatch重新赋值
                countDownLatch = new CountDownLatch(1);
                break;
            case NodeDataChanged:
                System.out.println("WatchedEvent 回调：节点数据被更改");
                //注意：如果数据被变更，则需要重新获取数据
                zooKeeper.getData(event.getPath(),this,this,"abc");
                break;
            case NodeChildrenChanged:
                break;
        }

    }


}

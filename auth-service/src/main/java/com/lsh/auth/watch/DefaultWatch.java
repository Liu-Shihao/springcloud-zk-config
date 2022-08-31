package com.lsh.auth.watch;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/15 23:33
 * @Desc: 监听客户端连接状态
 */
@Slf4j
public class DefaultWatch implements Watcher {

    ZooKeeper zooKeeper;

    CountDownLatch countDownLatch;

    public DefaultWatch(CountDownLatch countDownLatch,ZooKeeper zooKeeper) {
        this.countDownLatch = countDownLatch;
        this.zooKeeper = zooKeeper;
    }

    @Override
    public void process(WatchedEvent event) {

        switch (event.getState()) {
            case Unknown:
                break;
            case Disconnected:
                log.info("=======Zookeeper断开连接!=======");
                break;
            case NoSyncConnected:
                break;
            case SyncConnected:
                //如果收到了服务端的响应事件,连接成功
                countDownLatch.countDown();
                break;
            case AuthFailed:
                break;
            case ConnectedReadOnly:
                break;
            case SaslAuthenticated:
                break;
            case Expired:
                break;
        }

    }
}

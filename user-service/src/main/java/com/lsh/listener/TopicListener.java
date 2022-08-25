package com.lsh.listener;


import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/22 23:20
 * @Desc:
 */
public class TopicListener implements MessageListener<String> {

    @Override
    public void onMessage(Message<String> message) {
        String msg=message.getMessageObject();
        System.out.println("收到Topic消息："+msg);
    }
}

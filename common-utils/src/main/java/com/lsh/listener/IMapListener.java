package com.lsh.listener;


import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryAddedListener;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/22 23:19
 * @Desc:
 */
public class IMapListener implements EntryAddedListener<String, String> {

    @Override
    public void entryAdded(EntryEvent<String, String> event) {
        // TODO Auto-generated method stub
        //干你监听的操作
        System.out.println("MAP分布式监听："+event.getValue());
    }

}

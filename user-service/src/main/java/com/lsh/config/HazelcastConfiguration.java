package com.lsh.config;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;
import com.lsh.interceptor.IMapInterceptor;
import com.lsh.listener.IMapListener;
import com.lsh.listener.TopicListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/22 23:01
 * @Desc:
 */
@Configuration
public class HazelcastConfiguration {

    @Bean
    public Config hazelCastConfig() {
        Config config = new Config();
        //解决同网段下，不同库项目
        GroupConfig gc=new GroupConfig("hazelGroup");
        config.setInstanceName("hazelcast-instance")
                .addMapConfig(new MapConfig()
                        .setName("configuration")
                        // Map中存储条目的最大值[0~Integer.MAX_VALUE]。默认值为0。
                        .setMaxSizeConfig(new MaxSizeConfig(200, MaxSizeConfig.MaxSizePolicy.FREE_HEAP_SIZE))
                        //数据释放策略[NONE|LRU|LFU]。这是Map作为缓存的一个参数，用于指定数据的回收算法。默认为NONE。LRU：“最近最少使用“策略。
                        .setEvictionPolicy(EvictionPolicy.LRU)
                        //数据留存时间[0~Integer.MAX_VALUE]。缓存相关参数，单位秒，默认为0。
                        .setTimeToLiveSeconds(-1))
                .setGroupConfig(gc);
        return config;
    }

    /**
     * 添加Hazelcast监听器配置
     * @param config
     * @return
     */
    @Bean
    public HazelcastInstance hazelcastInstance(Config config) {
        HazelcastInstance hzInstance = Hazelcast.newHazelcastInstance(config);
        //分布式map监听
        IMap<Object, Object> imap = hzInstance.getMap("zk-cache");
        imap.addLocalEntryListener(new IMapListener());
        //拦截器（没写内容）
        imap.addInterceptor(new IMapInterceptor());
        //发布/订阅模式
        ITopic<String> topic = hzInstance.getTopic("hazelcastTopic");
        topic.addMessageListener(new TopicListener());

        return hzInstance;
    }

}

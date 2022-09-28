package com.citi.zk.config;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class HazelcastConfiguration {

    @Bean
    public Config hazelCastConfig() {
        Config config = new Config();
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
    @Bean("hazelcastInstance")
    public HazelcastInstance hazelcastInstance(Config config) throws Exception {
        HazelcastInstance hazelcast = Hazelcast.newHazelcastInstance(config);
        log.info("load com.hazelcast.core.HazelcastInstance.....");
        return hazelcast;
    }
}

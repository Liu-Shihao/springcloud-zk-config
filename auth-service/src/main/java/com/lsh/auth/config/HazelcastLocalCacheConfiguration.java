//package com.lsh.auth.config;
//
//import com.hazelcast.config.*;
//import com.hazelcast.core.Hazelcast;
//import com.hazelcast.core.HazelcastInstance;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.Map;
//
///**
// * @Author: LiuShihao
// * @Date: 2022/8/22 23:01
// * @Desc: 本地JVM缓存   3.12.5版本
// */
//@Configuration
//public class HazelcastLocalCacheConfiguration {
//
//    @Bean
//    public Config hazelCastConfig() {
//        Config config = new Config();
//        //解决同网段下，不同库项目
//        GroupConfig gc=new GroupConfig("hazelGroup");
//        config.setInstanceName("hazelcast-instance")
//                .addMapConfig(new MapConfig()
//                        .setName("configuration")
//                        // Map中存储条目的最大值[0~Integer.MAX_VALUE]。默认值为0。
//                        .setMaxSizeConfig(new MaxSizeConfig(200, MaxSizeConfig.MaxSizePolicy.FREE_HEAP_SIZE))
//                        //数据释放策略[NONE|LRU|LFU]。这是Map作为缓存的一个参数，用于指定数据的回收算法。默认为NONE。LRU：“最近最少使用“策略。
//                        .setEvictionPolicy(EvictionPolicy.LRU)
//                        //数据留存时间[0~Integer.MAX_VALUE]。缓存相关参数，单位秒，默认为0。
//                        .setTimeToLiveSeconds(-1))
//                .setGroupConfig(gc)
//                .getNetworkConfig().setPublicAddress("192.168.153.129");
//        return config;
//    }
//
//    /**
//     * @param config
//     * @return
//     */
//    @Bean("HazelcastClient")
//    public HazelcastInstance hazelcastInstance(Config config) {
//        HazelcastInstance hzInstance = Hazelcast.newHazelcastInstance(config);
//        Map<String, Object> cache = hzInstance.getMap("instruments");
//        cache.put("date", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
//        System.out.println("date :"+cache.get("date"));
//        System.out.println("1 :"+cache.get("1"));
//        return hzInstance;
//    }
//
//}

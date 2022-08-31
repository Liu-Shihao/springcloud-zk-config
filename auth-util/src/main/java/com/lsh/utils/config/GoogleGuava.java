package com.lsh.utils.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hazelcast.core.HazelcastInstance;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/26 09:50
 * @Desc: 本地缓存
 */
@Configuration
public class GoogleGuava {

    @Autowired
    HazelcastInstance hazelcastClient;

    @Autowired
    ZooKeeper zkClient;

    public static LoadingCache<String, Object> localCache = CacheBuilder.newBuilder()
            .initialCapacity(10)    //设置缓存初始大小
            .maximumSize(10000)       //设置最大容量
            .concurrencyLevel(5)    //并发数设置
            .expireAfterWrite(300, TimeUnit.SECONDS) //缓存过期时间    5分钟
            .expireAfterAccess(60,TimeUnit.SECONDS) // 此缓存对象经过多少秒没有被访问则过期。
            .recordStats() //统计缓存命中率
            .build(new MyCacheLoader());

    public static class MyCacheLoader extends CacheLoader<String, Object> {

        @Override
        public Object load(String s) throws Exception {
            //若数据存在则直接返回；若数据不存在，则根据ClassLoader的load方法加载数据至内存，然后返回该数据
            System.out.println("no cache...");
            return "";
        }
    }


}

package com.citi.zk;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/26 09:28
 * @Desc:
 */
@SpringBootTest
public class GuavaTest {

    @Test
    public void test() throws Exception {
        LoadingCache<String, Object> cache = CacheBuilder.newBuilder()
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())// 设置并发级别为cpu核心数，默认为4
                .initialCapacity(100)// 设置初始容量为100
                .maximumSize(1000)// 设置最大容量为1000
                .expireAfterWrite(15, TimeUnit.SECONDS)
                .build(new CacheLoader<String, Object>() {
                    @Override
                    public Object load(String s) throws Exception {
                        //若数据存在则直接返回；若数据不存在，则根据ClassLoader的load方法加载数据至内存，然后返回该数据
                        System.out.println("no cache");
                        return s+" no cache";
                    }
                });

        System.out.println("before get from cache :"+cache.get("test"));

        cache.put("test","hello guava!");

        System.out.println("get from cache :"+cache.get("test"));

        Thread.sleep(15000);

        System.out.println("after 15s :"+cache.get("test"));


    }
}

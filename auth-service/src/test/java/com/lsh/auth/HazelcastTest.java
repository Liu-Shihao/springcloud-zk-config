package com.lsh.auth;

import com.hazelcast.core.HazelcastInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/26 11:31
 * @Desc:
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class HazelcastTest {

    @Autowired
    HazelcastInstance hazelcastClient;

    @Test
    public void test(){
        //map缓存
        Map<String, Object> cache = hazelcastClient.getMap("instruments");
//        cache.put("date", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        System.out.println(cache.get("1"));

    }

}

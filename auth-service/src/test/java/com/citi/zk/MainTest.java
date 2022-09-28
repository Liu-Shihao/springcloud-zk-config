package com.citi.zk;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author: LiuShihao
 * @Date: 2022/9/23 14:57
 * @Desc:
 */
@SpringBootTest
public class MainTest {

    @Test
    public void test(){

        String str = "/policys/policy1/policy11";
        String[] split = str.split("/");
        System.out.println(split.length);
        for (String s : split) {
            System.out.println(s);
        }


    }
}

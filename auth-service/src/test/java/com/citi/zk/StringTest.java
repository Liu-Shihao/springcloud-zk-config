package com.citi.zk;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: LiuShihao
 * @Date: 2022/9/29 16:28
 * @Desc:
 */
@SpringBootTest
public class StringTest {
    @Test
    public void test(){
        String str = "/a/b/c";
        List<String> strings = splitSubPath(str);
        for (String string : strings) {
            System.out.println(string);
        }
    }

    public List<String> splitSubPath(String path){
        ArrayList<String> ans = new ArrayList<>();
        if (path == null){
            return ans;
        }
        String[] split = path.split("/");
        String subPath = "";
        for (int i = 1; i < split.length; i++) {
            subPath +="/"+split[i];
            ans.add(subPath);
        }
        return ans;
    }
}

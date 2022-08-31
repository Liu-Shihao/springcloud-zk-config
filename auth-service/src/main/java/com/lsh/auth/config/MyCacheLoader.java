package com.lsh.auth.config;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.CacheLoader;
import com.lsh.constant.ZKConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/31 12:07
 * @Desc:
 */
@Slf4j
@Component
public class MyCacheLoader extends CacheLoader<String, Set<String>> {

    @Autowired
    ZooKeeper zkClient;

    @Override
    public Set<String> load(String path) throws Exception {
        //若数据存在则直接返回；若数据不存在，则根据ClassLoader的load方法加载数据至内存，然后返回该数据
        System.out.println("本地缓存没有数据，从zk加载...");

        //判断是否存在该用户节点
        Stat exists = zkClient.exists(ZKConstant.ZK_USER_PATH + "/" + path, true);
        if (exists != null){
            Stat stat = new Stat();
            //获得用户的 roles 和 groups 信息
            byte[] data = zkClient.getData(ZKConstant.ZK_USER_PATH + "/" + path, true, stat);
            String s = new String(data);
            log.info("{} 该用户数据：{}",path,s);
            JSONObject jsonObject = JSONObject.parseObject(s);
            String userRole = jsonObject.getString(ZKConstant.ZK_USER_KEY_ROLES);
            String userGroup = jsonObject.getString(ZKConstant.ZK_USER_KEY_GROUP);
            List<String> roleArr = JSONObject.parseArray(userRole, String.class);
            List<String>  groupArr = JSONObject.parseArray(userGroup, String.class);

            HashSet<String> policysSet = new HashSet<>();
            //获得user的 roles 的policy信息
            for (String role : roleArr) {
                Stat stat1 = new Stat();
                byte[] data1 = zkClient.getData(ZKConstant.ZK_ROLE_PATH + "/" + role, true, stat1);
                List<String> policy1 = JSONObject.parseArray(new String(data1), String.class);
                policysSet.addAll(policy1);
            }
            //获得user的 policy 的policy信息
            for (String group : groupArr) {
                Stat stat1 = new Stat();
                byte[] data1 = zkClient.getData(ZKConstant.ZK_GROUP_PATH + "/" + group, true, stat1);
                List<String> policy2 = JSONObject.parseArray(new String(data1), String.class);
                policysSet.addAll(policy2);
            }

            HashSet<String> apiSet = new HashSet<>();
            //查询所有policys的apis
            for (String  policy: policysSet) {
                Stat stat1 = new Stat();
                byte[] data1 = zkClient.getData(ZKConstant.ZK_POLICY_PATH + "/" + policy, true, stat1);
                List<String> apis = JSONObject.parseArray(new String(data1), String.class);
                apiSet.addAll(apis);
            }
            log.info("{}:{}",path,apiSet);
            return apiSet;
        }
        log.info("{} 该用户zk节点中不存在...");
        return new HashSet<>();
    }
}
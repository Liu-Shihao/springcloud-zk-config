package com.citi.zk.service.impl;

import com.google.common.cache.LoadingCache;
import com.citi.zk.dto.UserInfo;
import com.citi.zk.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/30 15:13
 * @Desc:
 */
@Slf4j
@Service
public class AuthServiceImpl  implements AuthService {

    @Autowired
    LoadingCache<String, Set<String>> localCache;


    /**
     * 根据 businessCode-userId 查询groups和roles对应的policys，判断是否具有目标api的权限
     * @param userInfo
     * @return
     */
    @Override
    public Boolean authorize(UserInfo userInfo) throws Exception {
        String path = userInfo.getBusinessCode()+"-"+userInfo.getUserId() ;
        Set<String> apis = localCache.get(path);
        return apis.contains(userInfo.getTargetApi());
    }



}

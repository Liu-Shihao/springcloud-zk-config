package com.lsh.auth.service;

import com.lsh.auth.dto.UserInfo;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/30 15:13
 * @Desc:
 */
public interface AuthService {
    Boolean authorize(UserInfo userInfo) throws Exception;
}

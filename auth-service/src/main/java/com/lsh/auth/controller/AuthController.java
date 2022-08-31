package com.lsh.auth.controller;

import com.lsh.auth.dto.UserInfo;
import com.lsh.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/30 15:09
 * @Desc:
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService aUthService;

    @PostMapping
    public Boolean checkPermission(@RequestBody UserInfo userInfo) throws Exception {
        return aUthService.authorize(userInfo);
    }
}

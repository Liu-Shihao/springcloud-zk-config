package com.lsh.auth.dto.zk;

import lombok.Data;

import java.util.ArrayList;

/**
 * @Author: LiuShihao
 * @Date: 2022/9/8 13:49
 * @Desc:
 */
@Data
public class UserNode  {

    public ArrayList<String> roles;

    public ArrayList<String> groups;
}

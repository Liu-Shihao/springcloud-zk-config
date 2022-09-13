package com.lsh.auth.dto.zk;

import lombok.Data;

import java.util.ArrayList;

/**
 * @Author: LiuShihao
 * @Date: 2022/9/8 13:49
 * @Desc:
 */
@Data
public class PolicyNode  {

    public ArrayList<String> apis;
    public ArrayList<String> roles;
    public ArrayList<String> groups;
    public ArrayList<String> ancestors;
    public ArrayList<String> childrens;


}

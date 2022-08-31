package com.lsh.auth.dto;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/30 17:24
 * @Desc:
 */
@Data
public class ZkNode {

    public String path;

    public String type;

    //创建 group节点、role节点、policy节点传递节点值
    public List<String> arrays;

    //创建user时传递使用
    public List<String> roles;

    //创建user时传递使用
    public List<String> groups;


    public List<String> types = Arrays.asList("group","role","policy");

    enum NodeType{
        GROUP("group"),
        ROLE("role"),
        USER("user"),
        POLICY("policy");

        public String value;

        NodeType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }




}

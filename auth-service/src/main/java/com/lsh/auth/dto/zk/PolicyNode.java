package com.lsh.auth.dto.zk;

import com.lsh.auth.dto.ZkNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;

/**
 * @Author: LiuShihao
 * @Date: 2022/9/8 13:49
 * @Desc:
 */
@Data
@ApiModel("Policy Node Entity")
public class PolicyNode extends ZkNode {

    @ApiModelProperty("API List")
    public ArrayList<String> apis;

    @ApiModelProperty("Role List")
    public ArrayList<String> roles;

    @ApiModelProperty("Group List")
    public ArrayList<String> groups;

    @ApiModelProperty("Ancestors List")
    public ArrayList<String> ancestors;

    @ApiModelProperty("Childrens List")
    public ArrayList<String> childrens;


}

package com.citi.zk.dto.zk;

import com.citi.zk.dto.ZkNode;
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

    @ApiModelProperty("User List")
    public ArrayList<String> users;

}

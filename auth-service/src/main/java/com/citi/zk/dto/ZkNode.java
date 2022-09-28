package com.citi.zk.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/30 17:24
 * @Desc:
 */
@Data
@ApiModel("ZkNode")
public class ZkNode {

    @ApiModelProperty("Zookeeper Path")
    @NotNull(message = "path param not null")
    public String path;

    @Pattern(regexp = "^user|api|group|role|policy$",message = "type param illegal")
//    @NotNull(message = "type param not null")
    @ApiModelProperty("Node Type，value must match ：user|api|group|role|policy")
    public String type;

//    public UserNode userNode;
//
//    // role/group/api
//    public List<String> policys;
//
//    public PolicyNode policyNode;

}

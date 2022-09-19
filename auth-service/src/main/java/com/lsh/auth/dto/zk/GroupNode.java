package com.lsh.auth.dto.zk;

import com.lsh.auth.dto.ZkNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author: LiuShihao
 * @Date: 2022/9/16 10:12
 * @Desc:
 */
@Data
@ApiModel("Group Node Entity")
public class GroupNode extends ZkNode {

    @ApiModelProperty("Policy List")
    @NotNull(message = "policys not null")
    private List<String> policys;

    @ApiModelProperty("User List")
    private List<String> users;

}

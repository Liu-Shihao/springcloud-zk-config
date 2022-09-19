package com.lsh.auth.dto.zk;

import com.lsh.auth.dto.ZkNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;

/**
 * @Author: LiuShihao
 * @Date: 2022/9/8 13:49
 * @Desc:
 */
@Data
@ApiModel("User Node Entity")
public class UserNode extends ZkNode {

    @ApiModelProperty("Role List")
    @NotNull(message = "roles not null")
    public ArrayList<String> roles;

    @ApiModelProperty("Group List")
    @NotNull(message = "groups not null")
    public ArrayList<String> groups;
}

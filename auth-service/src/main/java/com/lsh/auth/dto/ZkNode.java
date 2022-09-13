package com.lsh.auth.dto;

import com.lsh.auth.dto.zk.PolicyNode;
import com.lsh.auth.dto.zk.UserNode;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/30 17:24
 * @Desc:
 */
@Data
public class ZkNode {

    @NotNull(message = "path param not null")
    public String path;

    @Pattern(regexp = "^user|api|group|role|policy$",message = "type param illegal")
    @NotNull(message = "type param not null")
    public String type;

    public UserNode userNode;

    // role/group/api
    public List<String> policys;

    public PolicyNode policyNode;

}

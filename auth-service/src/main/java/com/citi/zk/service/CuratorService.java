package com.citi.zk.service;

import com.citi.zk.dto.ZkNode;
import com.citi.zk.dto.zk.*;
import com.lsh.auth.dto.zk.*;

import java.util.HashMap;

/**
 * @Author: LiuShihao
 * @Date: 2022/9/27 14:49
 * @Desc:
 */
public interface CuratorService {
    String createUser(UserNode userNode);

    String createApi(APINode node);

    String createPolicy(PolicyNode policyNode);

    String createRole(RoleNode node);

    String createGroup(GroupNode node);

    String updateUser(UserNode userNode);

    String updateAPI(APINode node);

    String updatePolicy(PolicyNode policyNode);

    String updateRole(RoleNode node);

    String updateGroup(GroupNode node);

    String deleteUser(ZkNode node);

    String deletePolicy(ZkNode node);

    String deleteAPI(ZkNode node);

    String deleteGroup(ZkNode node);

    String deleteRole(ZkNode node);

    HashMap selectGroup(String groupId);

    HashMap selectRole(String roleId);

    HashMap selectUser(String userId);

    String getData(ZkNode node);

    String getPolicyPermission(String path);
}

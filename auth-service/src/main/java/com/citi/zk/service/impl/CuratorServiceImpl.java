package com.citi.zk.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.citi.zk.dto.zk.*;
import com.citi.zk.service.CuratorService;
import com.citi.zk.dto.ZkNode;
import com.lsh.auth.dto.zk.*;
import com.lsh.constant.ZKConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author: LiuShihao
 * @Date: 2022/9/27 14:50
 * @Desc:
 */
@Slf4j
@Service
public class CuratorServiceImpl implements CuratorService {

    @Autowired
    CuratorFramework curatorClient;

    @Override
    public String createUser(UserNode userNode) {
        String path = userNode.getPath();
        try {
            // check path
            checkUserData(userNode);
            // update group
            for (String group : userNode.getGroups()) {
                byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_GROUP_PATH + group);
                GroupNode groupNode = JSONObject.parseObject(new String(bytes), GroupNode.class);
                groupNode.getUsers().add(path);
                curatorClient.setData().forPath(ZKConstant.ZK_GROUP_PATH + group,JSONObject.toJSONString(groupNode).getBytes());
            }
            // update role
            for (String role : userNode.getRoles()) {
                byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_ROLE_PATH + role);
                RoleNode roleNode = JSONObject.parseObject(new String(bytes), RoleNode.class);
                roleNode.getUsers().add(path);
                curatorClient.setData().forPath(ZKConstant.ZK_ROLE_PATH + role,JSONObject.toJSONString(roleNode).getBytes());
            }
            // update policy
            for (String policy : userNode.getPolicys()) {
                byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH + policy);
                PolicyNode policyNode = JSONObject.parseObject(new String(bytes), PolicyNode.class);
                policyNode.getUsers().add(path);
                curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH + policy,JSONObject.toJSONString(policyNode).getBytes());
            }
            String data = JSON.toJSONString(userNode);
            log.info("create user node:{} : {}",path,data);
            curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(ZKConstant.ZK_USER_PATH+path,data.getBytes());
        }catch (Exception e){
            return e.getMessage();
        }
        return "success";
    }

    @Override
    public String createApi(APINode node) {
        String path = node.getPath();
        try {
            // check policy path
            for (String policy : node.getPolicys()) {
                if (!checkExists(ZKConstant.ZK_POLICY_PATH + policy)){
                    log.info("{} notexist！",ZKConstant.ZK_POLICY_PATH + policy);
                    return ZKConstant.ZK_POLICY_PATH + policy+" not exist！";
                }
            }
            // update policy
            for (String policy : node.getPolicys()) {
                byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH + policy);
                PolicyNode policyNode = JSONObject.parseObject(new String(bytes), PolicyNode.class);
                policyNode.getApis().add(path);
                curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH + policy,JSONObject.toJSONString(policyNode).getBytes());
            }
            String data = JSON.toJSONString(node);
            log.info("create api node : {} : {}",path,data);
            curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(ZKConstant.ZK_API_PATH+path,data.getBytes());
        }catch (Exception e){
            return e.getMessage();
        }
        return "success";
    }

    @Override
    public String createPolicy(PolicyNode policyNode) {
        String path = policyNode.getPath();
        try {
            // check path
            checkPolicyData(policyNode);
            // update api
            for (String api : policyNode.getApis()) {
                byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_API_PATH + api);
                APINode apiNode = JSONObject.parseObject(new String(bytes), APINode.class);
                apiNode.getPolicys().add(path);
                curatorClient.setData().forPath(ZKConstant.ZK_API_PATH + api,JSONObject.toJSONString(apiNode).getBytes());
            }
            //update group
            for (String group : policyNode.getGroups()) {
                byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_GROUP_PATH + group);
                GroupNode groupNode = JSONObject.parseObject(new String(bytes), GroupNode.class);
                groupNode.getPolicys().add(path);
                curatorClient.setData().forPath(ZKConstant.ZK_GROUP_PATH + group,JSONObject.toJSONString(groupNode).getBytes());
            }
            //update role
            for (String role : policyNode.getRoles()) {
                byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_ROLE_PATH + role);
                RoleNode roleNode = JSONObject.parseObject(new String(bytes), RoleNode.class);
                roleNode.getPolicys().add(path);
                curatorClient.setData().forPath(ZKConstant.ZK_ROLE_PATH + role,JSONObject.toJSONString(roleNode).getBytes());
            }
            //update user
            for (String user : policyNode.getUsers()) {
                byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH + user);
                UserNode userNode = JSONObject.parseObject(new String(bytes), UserNode.class);
                userNode.getPolicys().add(path);
                curatorClient.setData().forPath(ZKConstant.ZK_USER_PATH + user,JSONObject.toJSONString(userNode).getBytes());
            }

            String data = JSON.toJSONString(policyNode);
            log.info("create policy node : {} : {}",path,data);
            curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(ZKConstant.ZK_POLICY_PATH+path,data.getBytes());
        }catch (Exception e){
            log.error(e.getMessage());
            return e.getMessage();
        }
        return "success";
    }

    @Override
    public String createRole(RoleNode node) {
        String path = node.getPath();
        try {
            // check path
            checkPolicyAndUserPath(node.getPolicys(),node.getUsers());
            // update policy
            for (String policy : node.getPolicys()) {
                byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH + policy);
                PolicyNode policyNode = JSONObject.parseObject(new String(bytes), PolicyNode.class);
                policyNode.getRoles().add(path);
                curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH + policy,JSONObject.toJSONString(policyNode).getBytes());
            }

            // update user
            for (String user : node.getUsers()) {
                byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH + user);
                UserNode userNode = JSONObject.parseObject(new String(bytes), UserNode.class);
                userNode.getRoles().add(path);
                curatorClient.setData().forPath(ZKConstant.ZK_USER_PATH + user,JSONObject.toJSONString(userNode).getBytes());
            }
            String data = JSON.toJSONString(node);
            log.info("create role node : {} : {}",path,data);
            curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(ZKConstant.ZK_ROLE_PATH+path,data.getBytes());
        }catch (Exception e){
            return e.getMessage();
        }
        return "success";
    }

    @Override
    public String createGroup(GroupNode node) {
        String path = node.getPath();
        try {
            // check path
            checkPolicyAndUserPath(node.getPolicys(),node.getUsers());
            // update policy
            for (String policy : node.getPolicys()) {
                byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH + policy);
                PolicyNode policyNode = JSONObject.parseObject(new String(bytes), PolicyNode.class);
                if (!policyNode.getGroups().contains(path)){
                    policyNode.getGroups().add(path);
                    curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH + policy,JSONObject.toJSONString(policyNode).getBytes());
                }
            }
            // update user
            for (String user : node.getUsers()) {
                byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH + user);
                UserNode userNode = JSONObject.parseObject(new String(bytes), UserNode.class);
                if (!userNode.getRoles().contains(path)){
                    userNode.getRoles().add(path);
                    curatorClient.setData().forPath(ZKConstant.ZK_USER_PATH + user,JSONObject.toJSONString(userNode).getBytes());
                }
            }
            String data = JSON.toJSONString(node);
            log.info("create group node : {} : {}",path,data);
            curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(ZKConstant.ZK_GROUP_PATH+path,data.getBytes());
        }catch (Exception e){
            return e.getMessage();
        }
        return "success";
    }

    @Override
    public String updateUser(UserNode userNode) {
        String path = userNode.getPath();
        try {
            // check path
            checkUserData(userNode);
            // get old data
            byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH + path);
            UserNode old_user = JSONObject.parseObject(new String(bytes), UserNode.class);
            // update group data
            ArrayList<String> old_userGroups = old_user.getGroups();
            ArrayList<String> new_userGroups = userNode.getGroups();
            List<String> copy_oldGroup = new ArrayList<>(Arrays.asList(new String[old_userGroups.size()]));
            List<String> copy_newGroup = new ArrayList<>(Arrays.asList(new String[new_userGroups.size()]));
            Collections.copy(copy_oldGroup,old_userGroups);
            Collections.copy(copy_newGroup,new_userGroups);

            // remove new data from old ,need to remove
            copy_oldGroup.removeAll(new_userGroups);
            for (String oldGroup : copy_oldGroup) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_GROUP_PATH + oldGroup);
                GroupNode groupNode = JSONObject.parseObject(new String(bytes1), GroupNode.class);
                groupNode.getUsers().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_GROUP_PATH + oldGroup,JSONObject.toJSONString(groupNode).getBytes());
            }
            // remove old data from new ,need to add
            copy_newGroup.removeAll(old_userGroups);
            for (String newGroup : copy_newGroup) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_GROUP_PATH + newGroup);
                GroupNode groupNode = JSONObject.parseObject(new String(bytes1), GroupNode.class);
                groupNode.getUsers().add(path);
                curatorClient.setData().forPath(ZKConstant.ZK_GROUP_PATH + newGroup,JSONObject.toJSONString(groupNode).getBytes());
            }
            // updata role data
            ArrayList<String> old_userRoles = old_user.getRoles();
            ArrayList<String> new_userRoles = userNode.getRoles();
            List<String> copy_oldRoles = new ArrayList<>(Arrays.asList(new String[old_userRoles.size()]));
            List<String> copy_newRoles = new ArrayList<>(Arrays.asList(new String[new_userRoles.size()]));
            Collections.copy(copy_oldRoles,old_userRoles);
            Collections.copy(copy_newRoles,new_userRoles);
            // remove new data from old ,need to remove
            copy_oldRoles.removeAll(new_userRoles);
            for (String oldRole : copy_oldRoles) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_ROLE_PATH + oldRole);
                RoleNode roleNode = JSONObject.parseObject(new String(bytes1), RoleNode.class);
                roleNode.getUsers().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_ROLE_PATH + oldRole,JSONObject.toJSONString(roleNode).getBytes());
            }
            // remove old data from new ,need to add
            copy_newRoles.removeAll(old_userRoles);
            for (String newRole : copy_newRoles) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_ROLE_PATH + newRole);
                RoleNode roleNode = JSONObject.parseObject(new String(bytes1), RoleNode.class);
                roleNode.getUsers().add(path);
                curatorClient.setData().forPath(ZKConstant.ZK_ROLE_PATH + newRole,JSONObject.toJSONString(roleNode).getBytes());
            }
            // update policy
            ArrayList<String> old_userPolicys = old_user.getPolicys();
            ArrayList<String> new_userPolicys = userNode.getPolicys();
            List<String> copy_oldPolicys = new ArrayList<>(Arrays.asList(new String[old_userPolicys.size()]));
            List<String> copy_newPolicys = new ArrayList<>(Arrays.asList(new String[new_userPolicys.size()]));
            Collections.copy(copy_oldPolicys,old_userPolicys);
            Collections.copy(copy_newPolicys,new_userPolicys);
            copy_oldPolicys.removeAll(new_userPolicys);
            for (String copy_oldPolicy : copy_oldPolicys) {
                //need to remove
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH + copy_oldPolicy);
                PolicyNode policyNode = JSONObject.parseObject(new String(bytes1), PolicyNode.class);
                policyNode.getUsers().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH + copy_oldPolicy,JSONObject.toJSONString(policyNode).getBytes());

            }
            copy_newPolicys.removeAll(old_userPolicys);
            for (String new_userPolicy : copy_newPolicys) {
                //need to add
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH + new_userPolicy);
                PolicyNode policyNode = JSONObject.parseObject(new String(bytes1), PolicyNode.class);
                policyNode.getUsers().add(path);
                curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH + new_userPolicy,JSONObject.toJSONString(policyNode).getBytes());

            }
            // save new data
            String data = JSON.toJSONString(userNode);
            log.info("update user node : {} : {}",path,data);
            curatorClient.setData().forPath(ZKConstant.ZK_USER_PATH+path,data.getBytes());
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
        return "success";
    }

    @Override
    public String updateAPI(APINode node) {
        String path = node.getPath();
        // check policy path
        if (node.getPolicys() != null){
            for (String policy : node.getPolicys()) {
                if (!checkExists(ZKConstant.ZK_POLICY_PATH + policy)){
                    log.info("{} notexist！",ZKConstant.ZK_POLICY_PATH + policy);
                    return ZKConstant.ZK_POLICY_PATH + policy+" not exist！";
                }
            }
        }
        try {
            // get zk node old data
            byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_API_PATH +path);
            APINode apiNode = JSONObject.parseObject(new String(bytes1), APINode.class);
            List<String> oldPolicy = apiNode.getPolicys();
            List<String> newPolicy = node.getPolicys();
            List<String> copy_oldPolicy = new ArrayList<>(Arrays.asList(new String[oldPolicy.size()]));
            List<String> copy_newPolicy = new ArrayList<>(Arrays.asList(new String[newPolicy.size()]));
            Collections.copy(copy_oldPolicy,oldPolicy);
            Collections.copy(copy_newPolicy,newPolicy);

            copy_oldPolicy.removeAll(newPolicy);
            //remove new data from old data :  need remove
            for (String oldPolicy1 : copy_oldPolicy) {
                byte[] bytes2 = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH + oldPolicy1);
                PolicyNode policyNode1 = JSONObject.parseObject(new String(bytes2), PolicyNode.class);
                ArrayList<String> apis = policyNode1.getApis();
                apis.remove(node.getPath());
                curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH + oldPolicy1,JSONObject.toJSONString(policyNode1).getBytes());
            }
            //remove old data from new data :  need add
            copy_newPolicy.removeAll(oldPolicy);
            for (String newPolicy1 : copy_newPolicy) {
                byte[] bytes2 = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH + newPolicy1);
                PolicyNode policyNode1 = JSONObject.parseObject(new String(bytes2), PolicyNode.class);
                ArrayList<String> apis = policyNode1.getApis();
                apis.add(node.getPath());
                curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH + newPolicy1,JSONObject.toJSONString(policyNode1).getBytes());
            }
            // save new data
            String data = JSON.toJSONString(node);
            log.info("update api node : {} = {}",ZKConstant.ZK_API_PATH+path,data);
            curatorClient.setData().forPath(ZKConstant.ZK_API_PATH+path,data.getBytes());
            return "success";
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @Override
    public String updatePolicy(PolicyNode policyNode) {
        String path = policyNode.getPath();
        try {
            // check path
            checkPolicyData(policyNode);
            // get old data
            byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH+path);
            PolicyNode oldPolicyData = JSONObject.parseObject(new String(bytes), PolicyNode.class);
            // update api
            ArrayList<String> oldApi = oldPolicyData.getApis();
            ArrayList<String> newApi = policyNode.getApis();
            ArrayList<String> copy_oldApi = new ArrayList<>(Arrays.asList(new String[oldApi.size()]));
            ArrayList<String> copy_newApi = new ArrayList<>(Arrays.asList(new String[newApi.size()]));
            Collections.copy(copy_oldApi,oldApi);
            Collections.copy(copy_newApi,newApi);
            // remove new from old, need to del this policy
            copy_oldApi.removeAll(newApi);
            for (String copy_old : copy_oldApi) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_API_PATH + copy_old);
                APINode apiNode = JSONObject.parseObject(new String(bytes1), APINode.class);
                apiNode.getPolicys().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_API_PATH + copy_old,JSONObject.toJSONString(apiNode).getBytes());
            }
            // remove old from new,  need to add this policy
            copy_newApi.removeAll(oldApi);
            for (String copy_new : copy_newApi) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_API_PATH + copy_new);
                APINode apiNode = JSONObject.parseObject(new String(bytes1), APINode.class);
                apiNode.getPolicys().add(path);
                curatorClient.setData().forPath(ZKConstant.ZK_API_PATH + copy_new,JSONObject.toJSONString(apiNode).getBytes());
            }
            // update group
            ArrayList<String> oldGroup = oldPolicyData.getGroups();
            ArrayList<String> newGroup = policyNode.getGroups();
            ArrayList<String> copy_oldGroup = new ArrayList<>(Arrays.asList(new String[oldGroup.size()]));
            ArrayList<String> copy_newGroup = new ArrayList<>(Arrays.asList(new String[newGroup.size()]));
            Collections.copy(copy_oldGroup,oldGroup);
            Collections.copy(copy_newGroup,newGroup);
            // remove new from old , need to del this policy
            copy_oldGroup.removeAll(newGroup);
            for (String s : copy_oldGroup) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_GROUP_PATH + s);
                GroupNode groupNode = JSONObject.parseObject(new String(bytes1), GroupNode.class);
                groupNode.getPolicys().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_GROUP_PATH + s,JSONObject.toJSONString(groupNode).getBytes());
            }
            // remove old from new  ,  need to add this policy
            copy_newGroup.removeAll(oldGroup);
            for (String s : copy_newGroup) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_GROUP_PATH + s);
                GroupNode groupNode = JSONObject.parseObject(new String(bytes1), GroupNode.class);
                groupNode.getPolicys().add(path);
                curatorClient.setData().forPath(ZKConstant.ZK_GROUP_PATH + s,JSONObject.toJSONString(groupNode).getBytes());
            }
            // update role
            ArrayList<String> oldRole = oldPolicyData.getRoles();
            ArrayList<String> newRole = policyNode.getRoles();
            ArrayList<String> copy_oldRole = new ArrayList<>(Arrays.asList(new String[oldRole.size()]));
            ArrayList<String> copy_newRole = new ArrayList<>(Arrays.asList(new String[newRole.size()]));
            Collections.copy(copy_oldRole,oldRole);
            Collections.copy(copy_newRole,newRole);
            // remove new from old , need to del this policy
            copy_oldRole.removeAll(newRole);
            for (String s : copy_oldRole) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_ROLE_PATH + s);
                RoleNode roleNode = JSONObject.parseObject(new String(bytes1), RoleNode.class);
                roleNode.getPolicys().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_ROLE_PATH + s,JSONObject.toJSONString(roleNode).getBytes());
            }
            // remove old from new  ,  need to add this policy
            copy_newRole.removeAll(oldRole);
            for (String s : copy_newRole) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_ROLE_PATH + s);
                RoleNode roleNode = JSONObject.parseObject(new String(bytes1), RoleNode.class);
                roleNode.getPolicys().add(path);
                curatorClient.setData().forPath(ZKConstant.ZK_ROLE_PATH + s,JSONObject.toJSONString(roleNode).getBytes());
            }

            // update user
            ArrayList<String> oldPolicyDataUsers = oldPolicyData.getUsers();
            ArrayList<String> newPolicyDataUsers = policyNode.getUsers();
            ArrayList<String> copy_oldPolicyDataUsers = new ArrayList<>(Arrays.asList(new String[oldPolicyDataUsers.size()]));
            ArrayList<String> copy_newPolicyDataUsers = new ArrayList<>(Arrays.asList(new String[newPolicyDataUsers.size()]));
            Collections.copy(copy_oldPolicyDataUsers,oldPolicyDataUsers);
            Collections.copy(copy_newPolicyDataUsers,newPolicyDataUsers);
            copy_oldPolicyDataUsers.removeAll(newPolicyDataUsers);
            for (String copy_oldPolicyDataUser : copy_oldPolicyDataUsers) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH + copy_oldPolicyDataUser);
                UserNode userNode = JSONObject.parseObject(new String(bytes1), UserNode.class);
                userNode.getPolicys().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_USER_PATH + copy_oldPolicyDataUser,JSONObject.toJSONString(userNode).getBytes());
            }
            copy_newPolicyDataUsers.removeAll(oldPolicyDataUsers);
            for (String newPolicyDataUser : copy_newPolicyDataUsers) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH + newPolicyDataUser);
                UserNode userNode = JSONObject.parseObject(new String(bytes1), UserNode.class);
                userNode.getPolicys().add(path);
                curatorClient.setData().forPath(ZKConstant.ZK_USER_PATH + newPolicyDataUser,JSONObject.toJSONString(userNode).getBytes());
            }

            for (String newPolicyDataUser : newPolicyDataUsers) {
                curatorClient.setData().forPath(ZKConstant.ZK_USER_PATH + newPolicyDataUser,curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH + newPolicyDataUser));
            }

            // save policy new data
            String data = JSON.toJSONString(policyNode);
            log.info("update policy node : {} = {}",ZKConstant.ZK_POLICY_PATH+path,data);
            curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH+path,data.getBytes());
            return "success";
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @Override
    public String updateRole(RoleNode node) {
        String path = node.getPath();
        try {
            // check path
            checkPolicyAndUserPath(node.getPolicys(),node.getUsers());
            // get old data
            byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_ROLE_PATH+path);
            RoleNode roleNode = JSONObject.parseObject(new String(bytes), RoleNode.class);
            // update policy
            List<String> old_RolePolicys = roleNode.getPolicys();
            List<String> new_RolePolicys = node.getPolicys();
            ArrayList<String> copy_oldRolePolicys = new ArrayList<>(Arrays.asList(new String[old_RolePolicys.size()]));
            ArrayList<String> copy_newRolePolicys = new ArrayList<>(Arrays.asList(new String[new_RolePolicys.size()]));
            Collections.copy(copy_oldRolePolicys,old_RolePolicys);
            Collections.copy(copy_newRolePolicys,new_RolePolicys);

            // remove new data from old data ,need to remove
            copy_oldRolePolicys.removeAll(new_RolePolicys);
            for (String copy_oldRolePolicy : copy_oldRolePolicys) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH+copy_oldRolePolicy);
                PolicyNode policyNode = JSONObject.parseObject(new String(bytes1), PolicyNode.class);
                policyNode.getRoles().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH+copy_oldRolePolicy,JSONObject.toJSONString(policyNode).getBytes());
            }
            // remove old data from new data ,need to add
            copy_newRolePolicys.removeAll(old_RolePolicys);
            for (String copy_newRolePolicy : copy_newRolePolicys) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH+copy_newRolePolicy);
                PolicyNode policyNode = JSONObject.parseObject(new String(bytes1), PolicyNode.class);
                policyNode.getRoles().add(path);
                curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH+copy_newRolePolicy,JSONObject.toJSONString(policyNode).getBytes());
            }
            // update user
            List<String> old_users = roleNode.getUsers();
            List<String> new_users = node.getUsers();
            ArrayList<String> copy_old_users = new ArrayList<>(Arrays.asList(new String[old_users.size()]));
            ArrayList<String> copy_new_users = new ArrayList<>(Arrays.asList(new String[new_users.size()]));
            Collections.copy(copy_old_users,old_users);
            Collections.copy(copy_new_users,new_users);

            // remove new data from old data ,need to remove
            copy_old_users.remove(new_users);
            for (String copy_old_user : copy_old_users) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH+copy_old_user);
                UserNode userNode = JSONObject.parseObject(new String(bytes1), UserNode.class);
                userNode.getRoles().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_USER_PATH+copy_old_user,JSONObject.toJSONString(userNode).getBytes());
            }
            // 5.2 remove old data from new data ,need to add
            copy_new_users.remove(old_users);
            for (String copy_new_user : copy_new_users) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH+copy_new_user);
                UserNode userNode = JSONObject.parseObject(new String(bytes1), UserNode.class);
                userNode.getRoles().add(path);
                curatorClient.setData().forPath(ZKConstant.ZK_USER_PATH+copy_new_user,JSONObject.toJSONString(userNode).getBytes());
            }
            for (String new_user : new_users) {
                curatorClient.setData().forPath(ZKConstant.ZK_USER_PATH+new_user,curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH+new_user));
            }

            String data = JSON.toJSONString(node);
            log.info("update role node : {} = {}",ZKConstant.ZK_ROLE_PATH+path,data);
            curatorClient.setData().forPath(ZKConstant.ZK_ROLE_PATH+path,data.getBytes());
            return "success";
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }


    }

    @Override
    public String updateGroup(GroupNode node) {
        String path = node.getPath();
        try {
            // check path
            checkPolicyAndUserPath(node.getPolicys(),node.getUsers());
            // get old data
            byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_GROUP_PATH+path);
            GroupNode groupNode = JSONObject.parseObject(new String(bytes), GroupNode.class);

            // update policy data
            List<String> old_RolePolicys = groupNode.getPolicys();
            List<String> new_RolePolicys = node.getPolicys();
            ArrayList<String> copy_oldRolePolicys = new ArrayList<>(Arrays.asList(new String[old_RolePolicys.size()]));
            ArrayList<String> copy_newRolePolicys = new ArrayList<>(Arrays.asList(new String[new_RolePolicys.size()]));
            Collections.copy(copy_oldRolePolicys,old_RolePolicys);
            Collections.copy(copy_newRolePolicys,new_RolePolicys);

            // remove new data from old data ,need to remove
            copy_oldRolePolicys.removeAll(new_RolePolicys);
            for (String copy_oldRolePolicy : copy_oldRolePolicys) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH+copy_oldRolePolicy);
                PolicyNode policyNode = JSONObject.parseObject(new String(bytes1), PolicyNode.class);
                policyNode.getGroups().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH+copy_oldRolePolicy,JSONObject.toJSONString(policyNode).getBytes());
            }
            // remove old data from new data ,need to add
            copy_newRolePolicys.removeAll(old_RolePolicys);
            for (String copy_newRolePolicy : copy_newRolePolicys) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH+copy_newRolePolicy);
                PolicyNode policyNode = JSONObject.parseObject(new String(bytes1), PolicyNode.class);
                policyNode.getGroups().add(path);
                curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH+copy_newRolePolicy,JSONObject.toJSONString(policyNode).getBytes());
            }
            // update user
            List<String> old_users = groupNode.getUsers();
            List<String> new_users = node.getUsers();
            ArrayList<String> copy_old_users = new ArrayList<>(Arrays.asList(new String[old_users.size()]));
            ArrayList<String> copy_new_users = new ArrayList<>(Arrays.asList(new String[new_users.size()]));
            Collections.copy(copy_old_users,old_users);
            Collections.copy(copy_new_users,new_users);

            // remove new data from old data ,need to remove
            copy_old_users.remove(new_users);
            for (String copy_old_user : copy_old_users) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH+copy_old_user);
                UserNode userNode = JSONObject.parseObject(new String(bytes1), UserNode.class);
                userNode.getGroups().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_USER_PATH+copy_old_user,JSONObject.toJSONString(userNode).getBytes());
            }
            // remove old data from new data ,need to add
            copy_new_users.remove(old_users);
            for (String copy_new_user : copy_new_users) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH+copy_new_user);
                UserNode userNode = JSONObject.parseObject(new String(bytes1), UserNode.class);
                userNode.getGroups().add(path);
                curatorClient.setData().forPath(ZKConstant.ZK_USER_PATH+copy_new_user,JSONObject.toJSONString(userNode).getBytes());
            }

            for (String new_user : new_users) {
                curatorClient.setData().forPath(ZKConstant.ZK_USER_PATH+new_user,curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH+new_user));
            }
            String data = JSON.toJSONString(node);
            log.info("update group node : {} = {}",ZKConstant.ZK_GROUP_PATH+path,data);
            curatorClient.setData().forPath(ZKConstant.ZK_GROUP_PATH+path,data.getBytes());
            return "success";
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @Override
    public String deleteUser(ZkNode node) {
        String path = node.getPath();
        try {
            // get old data
            byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH+path);
            UserNode userNode = JSONObject.parseObject(new String(bytes1), UserNode.class);
            // remove this user from group date
            for (String userNodeGroup : userNode.getGroups()) {
                byte[] bytes2 = curatorClient.getData().forPath(ZKConstant.ZK_GROUP_PATH+userNodeGroup);
                GroupNode groupNode = JSONObject.parseObject(new String(bytes2), GroupNode.class);
                groupNode.getUsers().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_GROUP_PATH+userNodeGroup,JSONObject.toJSONString(groupNode).getBytes());
            }
            // remove this user from role date
            for (String userNodeRole : userNode.getRoles()) {
                byte[] bytes2 = curatorClient.getData().forPath(ZKConstant.ZK_ROLE_PATH+userNodeRole);
                RoleNode roleNode = JSONObject.parseObject(new String(bytes2), RoleNode.class);
                roleNode.getUsers().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_ROLE_PATH+userNodeRole,JSONObject.toJSONString(roleNode).getBytes());
            }
            // remove this user from policys
            for (String policy : userNode.getPolicys()) {
                byte[] bytes2 = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH+policy);
                PolicyNode policyNode = JSONObject.parseObject(new String(bytes2), PolicyNode.class);
                policyNode.getUsers().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH+policy,JSONObject.toJSONString(policyNode).getBytes());
            }

            // delete this user path
            log.info("delete user node : {} = {}",ZKConstant.ZK_USER_PATH+path,userNode);
            curatorClient.delete().forPath(ZKConstant.ZK_USER_PATH+path);
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
        return "success";
    }

    @Override
    public String deletePolicy(ZkNode node) {
        String path = node.getPath();
        try {
            // get old data
            byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH + path);
            PolicyNode policyNode = JSONObject.parseObject(new String(bytes), PolicyNode.class);

            // remove this policy from apis
            for (String api : policyNode.getApis()) {
                byte[] bytes2 = curatorClient.getData().forPath(ZKConstant.ZK_API_PATH + api);
                APINode apiNode = JSONObject.parseObject(new String(bytes2), APINode.class);
                apiNode.getPolicys().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_API_PATH + api,JSONObject.toJSONString(apiNode).getBytes());
            }
            // remove this policy from group
            for (String group : policyNode.getGroups()) {
                byte[] bytes2 = curatorClient.getData().forPath(ZKConstant.ZK_GROUP_PATH + group);
                GroupNode groupNode = JSONObject.parseObject(new String(bytes2), GroupNode.class);
                groupNode.getPolicys().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_GROUP_PATH + group,JSONObject.toJSONString(groupNode).getBytes());
            }
            // remove this policy from role
            for (String role : policyNode.getRoles()) {
                byte[] bytes2 = curatorClient.getData().forPath(ZKConstant.ZK_ROLE_PATH + role);
                RoleNode roleNode = JSONObject.parseObject(new String(bytes2), RoleNode.class);
                roleNode.getPolicys().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_ROLE_PATH + role,JSONObject.toJSONString(roleNode).getBytes());
            }
            // remove it from user
            for (String user : policyNode.getUsers()) {
                byte[] bytes2 = curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH + user);
                UserNode userNode = JSONObject.parseObject(new String(bytes2), UserNode.class);
                userNode.getPolicys().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_USER_PATH + user,JSONObject.toJSONString(userNode).getBytes());
            }
            // delete this policy path
            log.info("delete policy node : {} = {}",ZKConstant.ZK_POLICY_PATH+path,policyNode);
            curatorClient.delete().forPath(ZKConstant.ZK_POLICY_PATH+path);
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
        return "success";
    }

    @Override
    public String deleteAPI(ZkNode node) {
        try {
            String path = node.getPath();
            // get old data
            byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_API_PATH + node.getPath());
            APINode apiNode = JSONObject.parseObject(new String(bytes1), APINode.class);

            // remove api from policys
            for (String policy : apiNode.getPolicys()) {
                byte[] bytes2 = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH + policy);
                PolicyNode policyNode = JSONObject.parseObject(new String(bytes2), PolicyNode.class);
                policyNode.getApis().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH + policy,JSONObject.toJSONString(policyNode).getBytes());
            }
            // delete this api path
            log.info("delete api node : {} = {}",ZKConstant.ZK_API_PATH+path,apiNode);
            curatorClient.delete().forPath(ZKConstant.ZK_API_PATH+path);
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
        return "success";
    }

    @Override
    public String deleteGroup(ZkNode node) {
        try {
            String path = node.getPath();
            // get old data
            byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_GROUP_PATH + path);
            GroupNode groupNode = JSONObject.parseObject(new String(bytes), GroupNode.class);
            // remove group from policys
            for (String policy : groupNode.getPolicys()) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH + policy);
                PolicyNode policyNode = JSONObject.parseObject(new String(bytes1), PolicyNode.class);
                policyNode.getGroups().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH + policy,JSONObject.toJSONString(policyNode).getBytes());
            }
            // remove group from users
            for (String user : groupNode.getUsers()) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH + user);
                UserNode userNode = JSONObject.parseObject(new String(bytes1), UserNode.class);
                userNode.getGroups().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_USER_PATH + user,JSONObject.toJSONString(userNode).getBytes());
            }
            // delete this  group path
            log.info("delete group node : {} = {}",ZKConstant.ZK_GROUP_PATH+path,groupNode);
            curatorClient.delete().forPath(ZKConstant.ZK_GROUP_PATH+path);
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
        return "success";
    }

    @Override
    public String deleteRole(ZkNode node) {
        try {
            String path = node.getPath();
            // get old data
            byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_ROLE_PATH + path);
            RoleNode roleNode = JSONObject.parseObject(new String(bytes), RoleNode.class);
            // remove role from policys
            for (String policy : roleNode.getPolicys()) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH + policy);
                PolicyNode policyNode = JSONObject.parseObject(new String(bytes1), PolicyNode.class);
                policyNode.getRoles().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH + policy,JSONObject.toJSONString(policyNode).getBytes());
            }
            // remove role from users
            for (String user : roleNode.getUsers()) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH + user);
                UserNode userNode = JSONObject.parseObject(new String(bytes1), UserNode.class);
                userNode.getRoles().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_USER_PATH + user,JSONObject.toJSONString(userNode).getBytes());
            }
            // delete this role path
            log.info("delete group node : {} = {}",ZKConstant.ZK_ROLE_PATH+path,roleNode);
            curatorClient.delete().forPath(ZKConstant.ZK_ROLE_PATH+path);
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
        return "success";
    }

    @Override
    public HashMap selectGroup(String groupId) {
        HashMap<String, Object> result = new HashMap<>();
        try {
            byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_GROUP_PATH + "/" + groupId);
            GroupNode groupNode = JSONObject.parseObject(new String(bytes), GroupNode.class);
            result.put("groupId",groupNode.getPath());
            result.put("policys",groupNode.getPolicys());
            result.put("users",groupNode.getUsers());
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return result;
    }

    @Override
    public HashMap selectRole(String roleId) {
        HashMap<String, Object> result = new HashMap<>();
        try {
            byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_ROLE_PATH + "/" + roleId);
            RoleNode roleNode = JSONObject.parseObject(new String(bytes), RoleNode.class);
            result.put("roleId",roleNode.getPath());
            result.put("policys",roleNode.getPolicys());
            result.put("users",roleNode.getUsers());
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return result;
    }

    @Override
    public HashMap selectUser(String userId) {
        HashMap<String, Object> result = new HashMap<>();
        try {
            byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH + "/" + userId);
            UserNode userNode = JSONObject.parseObject(new String(bytes), UserNode.class);

            HashSet<String> userPolicy = new HashSet<>();
            for (String group : userNode.getGroups()) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_GROUP_PATH + "/" + group);
                GroupNode groupNode = JSONObject.parseObject(new String(bytes1), GroupNode.class);
                userPolicy.addAll(groupNode.getPolicys());
            }
            for (String role : userNode.getRoles()) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_ROLE_PATH + "/" + role);
                RoleNode roleNode = JSONObject.parseObject(new String(bytes1), RoleNode.class);
                userPolicy.addAll(roleNode.getPolicys());
            }

            HashSet<String> userApi = new HashSet<>();
            for (String policy : userPolicy) {
                byte[] bytes3 = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH +  policy);
                PolicyNode policyNode = JSONObject.parseObject(new String(bytes3), PolicyNode.class);
                ArrayList<String> apis = policyNode.getApis();
                userApi.addAll(apis);
            }
            result.put("userId",userId);
            result.put("groups",userNode.getGroups());
            result.put("roles",userNode.getRoles());
            result.put("policys",userPolicy);
            result.put("apis",userApi);
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return result;
    }

    @Override
    public String getData(ZkNode node) {
        String result;
        String path = node.getPath();
        try {
            switch (node.getType()){
                case "user":
                    if (!path.startsWith(ZKConstant.ZK_USER_PATH)){
                        path = ZKConstant.ZK_USER_PATH+path;
                    }
                    break;
                case "role":
                    if (!path.startsWith(ZKConstant.ZK_ROLE_PATH)){
                        path = ZKConstant.ZK_ROLE_PATH+path;
                    }
                    break;
                case "group":
                    if (!path.startsWith(ZKConstant.ZK_GROUP_PATH)){
                        path = ZKConstant.ZK_GROUP_PATH+path;
                    }
                    break;
                case "policy":
                    if (!path.startsWith(ZKConstant.ZK_POLICY_PATH)){
                        path = ZKConstant.ZK_POLICY_PATH+path;
                    }
                    break;
                case "api":
                    if (!path.startsWith(ZKConstant.ZK_API_PATH)){
                        path = ZKConstant.ZK_API_PATH+path;
                    }
                    break;
                default:
                    break;
            }
            byte[] bytes = curatorClient.getData().forPath(path);
            result = new String(bytes);
        }catch (Exception e){
            log.error(e.getMessage());
            result = e.getMessage();
        }

        return result;
    }

    @Override
    public String getPolicyPermission(String path) {
        HashSet<String> ans = new HashSet<>();
        findPolicyPermission(ZKConstant.ZK_POLICY_PATH+"/"+path,ans);
        return ans.toString();
    }

    public void findPolicyPermission(String path ,HashSet<String> ans) {
        try {
            byte[] bytes = curatorClient.getData().forPath(path);
            PolicyNode policyNode = JSONObject.parseObject(new String(bytes), PolicyNode.class);
            ans.addAll(policyNode.getApis());
            List<String> childrenPolicys = curatorClient.getChildren().forPath(path);
            if (childrenPolicys == null || childrenPolicys.size() == 0){
                return;
            }
            for (String childrenPolicy : childrenPolicys) {
                findPolicyPermission(path+"/"+childrenPolicy,ans);
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }

    public void checkUserData(UserNode userNode){
        //check group path
        if (userNode.getGroups() != null && !userNode.getGroups().isEmpty()){
            for (String group : userNode.getGroups()) {
                if (!checkExists(ZKConstant.ZK_GROUP_PATH+group)){
                    log.info("{} not exist！",ZKConstant.ZK_GROUP_PATH + group);
                    throw new RuntimeException(ZKConstant.ZK_GROUP_PATH + group+" not exist！");
                }
            }
        }
        if (userNode.getRoles() != null && !userNode.getRoles().isEmpty()){
            //check role path
            for (String role : userNode.getRoles()) {
                if (!checkExists(ZKConstant.ZK_ROLE_PATH+role)){
                    log.info("{} not exist！",ZKConstant.ZK_ROLE_PATH+role);
                    throw new RuntimeException(ZKConstant.ZK_ROLE_PATH + role+" not exist！");
                }
            }
        }
        if (userNode.getPolicys() != null && !userNode.getPolicys().isEmpty()){
            //check policy path
            for (String policy : userNode.getPolicys()) {
                if (!checkExists(ZKConstant.ZK_POLICY_PATH+policy)){
                    log.info("{} not exist！",ZKConstant.ZK_POLICY_PATH+policy);
                    throw new RuntimeException(ZKConstant.ZK_POLICY_PATH + policy+" not exist！");
                }
            }
        }
    }

    public void checkPolicyData(PolicyNode policyNode){
        // check api path
        if (policyNode.getApis() != null && !policyNode.getApis().isEmpty()){
            for (String api : policyNode.getApis()) {
                if (!checkExists(ZKConstant.ZK_API_PATH + api)){
                    log.info("{} notexist！",ZKConstant.ZK_API_PATH + api);
                    throw new RuntimeException(ZKConstant.ZK_API_PATH + api+" not exist！");
                }
            }
        }
       if (policyNode.getGroups() != null && !policyNode.getGroups().isEmpty()){
           // check group path
           for (String group : policyNode.getGroups()) {
               if (!checkExists(ZKConstant.ZK_GROUP_PATH + group)){
                   log.info("{} notexist！",ZKConstant.ZK_GROUP_PATH + group);
                   throw new RuntimeException(ZKConstant.ZK_GROUP_PATH + group+" not exist！");
               }
           }
       }
       if (policyNode.getRoles() != null && !policyNode.getRoles().isEmpty()){
           // check role path
           for (String role : policyNode.getRoles()) {
               if (!checkExists(ZKConstant.ZK_ROLE_PATH + role)){
                   log.info("{} notexist！",ZKConstant.ZK_ROLE_PATH + role);
                   throw new RuntimeException(ZKConstant.ZK_ROLE_PATH + role+" not exist！");
               }
           }
       }
       if (policyNode.getUsers() != null && !policyNode.getUsers().isEmpty()){
           //check user path
           for (String user : policyNode.getUsers()) {
               if (!checkExists(ZKConstant.ZK_USER_PATH + user)){
                   log.info("{} notexist！",ZKConstant.ZK_USER_PATH + user);
                   throw new RuntimeException(ZKConstant.ZK_USER_PATH + user+" not exist！");
               }
           }
       }
    }

    public void checkPolicyAndUserPath(List<String> policys,List<String> users){
        // check policy path
        if (policys!= null && !policys.isEmpty()){
            for (String policy : policys) {
                if (!checkExists(ZKConstant.ZK_POLICY_PATH + policy)){
                    log.info("{} notexist！",ZKConstant.ZK_POLICY_PATH + policy);
                    throw new RuntimeException(ZKConstant.ZK_POLICY_PATH + policy+" not exist!");
                }
            }
        }
        // check user path
        if (users != null&& !users.isEmpty()){
            for (String user : users) {
                if (!checkExists(ZKConstant.ZK_USER_PATH + user)){
                    log.info("{} notexist！",ZKConstant.ZK_USER_PATH + user);
                    throw new RuntimeException(ZKConstant.ZK_USER_PATH + user+" not exist!");
                }
            }
        }
    }

    public boolean checkExists(String path){
        boolean bool = true;
        try {
            Stat stat = curatorClient.checkExists().forPath(path);
            if (stat == null){
                bool = false;
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return bool;
    }
}

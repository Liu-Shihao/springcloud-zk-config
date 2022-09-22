package com.lsh.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lsh.auth.dto.ZkNode;
import com.lsh.auth.dto.zk.*;
import com.lsh.constant.ZKConstant;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/curator")
public class CuratorController {

    @Autowired
    CuratorFramework curatorClient;

    /**
     * @param userNode
     * @return
     * @throws Exception
     */
    @ApiOperation("Create User")
    @PostMapping("/createUser")
    public String createUser(@RequestBody @Validated @ApiParam("UserNode Info") UserNode userNode)  {
        String path = userNode.getPath();
        try {
            //1. check GROUP path isExists?
            for (String group : userNode.getGroups()) {
                if (!checkExists(ZKConstant.ZK_GROUP_PATH+group)){
                    log.info("{} not exist！",ZKConstant.ZK_GROUP_PATH + group);
                    return ZKConstant.ZK_GROUP_PATH + group+" not exist！";
                }
            }
            //2. check ROLE path isExists?
            for (String role : userNode.getRoles()) {
                if (!checkExists(ZKConstant.ZK_ROLE_PATH+role)){
                    log.info("{} not exist！",ZKConstant.ZK_ROLE_PATH+role);
                    return ZKConstant.ZK_ROLE_PATH + role+" not exist！";
                }
            }
            //3. update group ,if group not contains this user, add user and save
            for (String group : userNode.getGroups()) {
                byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_GROUP_PATH + group);
                GroupNode groupNode = JSONObject.parseObject(new String(bytes), GroupNode.class);
                if (!groupNode.getUsers().contains(path)){
                    groupNode.getUsers().add(path);
                    curatorClient.setData().forPath(ZKConstant.ZK_GROUP_PATH + group,JSONObject.toJSONString(groupNode).getBytes());
                }
            }
            //4. update role ,if role not contains this user, add user and save
            for (String role : userNode.getRoles()) {
                byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_ROLE_PATH + role);
                RoleNode roleNode = JSONObject.parseObject(new String(bytes), RoleNode.class);
                if (!roleNode.getUsers().contains(path)){
                    roleNode.getUsers().add(path);
                    curatorClient.setData().forPath(ZKConstant.ZK_ROLE_PATH + role,JSONObject.toJSONString(roleNode).getBytes());
                }
            }
            String data = JSON.toJSONString(userNode);
            log.info("===>{} : {}",path,data);

            curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(ZKConstant.ZK_USER_PATH+path,data.getBytes());
        }catch (Exception e){
            return e.getMessage();
        }
        return "success";
    }

    @ApiOperation("Create API")
    @PostMapping("/createAPI")
    public String createApi(@RequestBody @Validated APINode node){
        String path = node.getPath();
        try {
            // 1. check policy path is exists
            for (String policy : node.getPolicys()) {
                if (!checkExists(ZKConstant.ZK_POLICY_PATH + policy)){
                    log.info("{} notexist！",ZKConstant.ZK_POLICY_PATH + policy);
                    return ZKConstant.ZK_POLICY_PATH + policy+" not exist！";
                }
            }
            // 2. update policy data if not contains current api
            for (String policy : node.getPolicys()) {
                byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH + policy);
                PolicyNode policyNode = JSONObject.parseObject(new String(bytes), PolicyNode.class);
                if (!policyNode.getApis().contains(path)){
                    policyNode.getApis().add(path);
                    curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH + policy,JSONObject.toJSONString(policyNode).getBytes());
                }
            }
            String data = JSON.toJSONString(node);
            log.info("===>{} : {}",path,data);
            curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(ZKConstant.ZK_API_PATH+path,data.getBytes());
        }catch (Exception e){
            return e.getMessage();
        }
        return "success";
    }

    @ApiOperation("Create Policy")
    @PostMapping("/createPolicy")
    public String createPolicy(@RequestBody @Validated PolicyNode policyNode){
        String path = policyNode.getPath();
        try {
            //1.first check api is exist？
            for (String api : policyNode.getApis()) {
                if (!checkExists(ZKConstant.ZK_API_PATH + api)){
                    log.info("{} notexist！",ZKConstant.ZK_API_PATH + api);
                    return ZKConstant.ZK_API_PATH + api+" not exist！";
                }
            }
            // 2. check group
            for (String group : policyNode.getGroups()) {
                if (!checkExists(ZKConstant.ZK_GROUP_PATH + group)){
                    log.info("{} notexist！",ZKConstant.ZK_GROUP_PATH + group);
                    return ZKConstant.ZK_GROUP_PATH + group+" not exist！";
                }
            }

            // 3. check role
            for (String role : policyNode.getRoles()) {
                if (!checkExists(ZKConstant.ZK_ROLE_PATH + role)){
                    log.info("{} notexist！",ZKConstant.ZK_ROLE_PATH + role);
                    return ZKConstant.ZK_ROLE_PATH + role+" not exist！";
                }
            }

            // 4. update api
            for (String api : policyNode.getApis()) {
                byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_API_PATH + api);
                APINode apiNode = JSONObject.parseObject(new String(bytes), APINode.class);
                if (!apiNode.getPolicys().contains(path)){
                    apiNode.getPolicys().add(path);
                    curatorClient.setData().forPath(ZKConstant.ZK_API_PATH + api,JSONObject.toJSONString(apiNode).getBytes());
                }
            }
            // 5、update group ,if group data not contains this policy ,add this & save
            for (String group : policyNode.getGroups()) {
                byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_GROUP_PATH + group);
                GroupNode groupNode = JSONObject.parseObject(new String(bytes), GroupNode.class);
                if (!groupNode.getPolicys().contains(path)){
                    groupNode.getPolicys().add(path);
                    curatorClient.setData().forPath(ZKConstant.ZK_GROUP_PATH + group,JSONObject.toJSONString(groupNode).getBytes());
                }
            }

            // 6. update role ,if roleNode Data not contains this policy ,add this & save
            for (String role : policyNode.getRoles()) {
                byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_ROLE_PATH + role);
                RoleNode roleNode = JSONObject.parseObject(new String(bytes), RoleNode.class);
                if (!roleNode.getPolicys().contains(path)){
                    roleNode.getPolicys().add(path);
                    curatorClient.setData().forPath(ZKConstant.ZK_ROLE_PATH + role,JSONObject.toJSONString(roleNode).getBytes());
                }
            }

            String data = JSON.toJSONString(policyNode);
            log.info("===>{} : {}",path,data);
            curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(ZKConstant.ZK_POLICY_PATH+path,data.getBytes());

            //register watcher
//            ZookeeperWatches watches = new ZookeeperWatches(curatorClient);
//            watches.znodeWatcher(ZKConstant.ZK_POLICY_PATH+path);
//            watches.znodeChildrenWatcher(ZKConstant.ZK_POLICY_PATH+path);

        }catch (Exception e){
            return e.getMessage();
        }
        return "success";
    }

    @ApiOperation("Create Role")
    @PostMapping("/createRole")
    public String createRole(@RequestBody @Validated RoleNode node){
        String path = node.getPath();
        try {
            // 1. check policy path is exists
            if (node.getPolicys() != null){
                for (String policy : node.getPolicys()) {
                    if (!checkExists(ZKConstant.ZK_POLICY_PATH + policy)){
                        log.info("{} notexist！",ZKConstant.ZK_POLICY_PATH + policy);
                        return ZKConstant.ZK_POLICY_PATH + policy+" not exist！";
                    }
                }
            }
            // 2. check user path is exists
            if (node.getUsers() != null){
                for (String user : node.getUsers()) {
                    if (!checkExists(ZKConstant.ZK_USER_PATH + user)){
                        log.info("{} notexist！",ZKConstant.ZK_USER_PATH + user);
                        return ZKConstant.ZK_USER_PATH + user+" not exist！";
                    }
                }
            }
            //3. update policy
            for (String policy : node.getPolicys()) {
                byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH + policy);
                PolicyNode policyNode = JSONObject.parseObject(new String(bytes), PolicyNode.class);
                if (!policyNode.getRoles().contains(path)){
                    policyNode.getRoles().add(path);
                    curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH + policy,JSONObject.toJSONString(policyNode).getBytes());
                }
            }

            // 4.update user
            for (String user : node.getUsers()) {
                byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH + user);
                UserNode userNode = JSONObject.parseObject(new String(bytes), UserNode.class);
                if (!userNode.getRoles().contains(path)){
                    userNode.getRoles().add(path);
                    curatorClient.setData().forPath(ZKConstant.ZK_USER_PATH + user,JSONObject.toJSONString(userNode).getBytes());
                }
            }
            String data = JSON.toJSONString(node);
            log.info("===>{} : {}",path,data);
            curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(ZKConstant.ZK_ROLE_PATH+path,data.getBytes());
        }catch (Exception e){
            return e.getMessage();
        }
        return "success";
    }

    @ApiOperation("Create Group")
    @PostMapping("/createGroup")
    public String createGroup(@RequestBody @Validated GroupNode node){
        String path = node.getPath();
        try {
            // 1. check policy is exists
            if (node.getPolicys() != null){
                for (String policy : node.getPolicys()) {
                    if (!checkExists(ZKConstant.ZK_POLICY_PATH + policy)){
                        log.info("{} notexist！",ZKConstant.ZK_POLICY_PATH + policy);
                        return ZKConstant.ZK_POLICY_PATH + policy+" not exist！";
                    }
                }
            }
            // 2. check user path is exists
            if (node.getUsers() != null){
                for (String user : node.getUsers()) {
                    if (!checkExists(ZKConstant.ZK_USER_PATH + user)){
                        log.info("{} notexist！",ZKConstant.ZK_USER_PATH + user);
                        return ZKConstant.ZK_USER_PATH + user+" not exist！";
                    }
                }
            }
            //3. update policy
            for (String policy : node.getPolicys()) {
                byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH + policy);
                PolicyNode policyNode = JSONObject.parseObject(new String(bytes), PolicyNode.class);
                if (!policyNode.getGroups().contains(path)){
                    policyNode.getGroups().add(path);
                    curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH + policy,JSONObject.toJSONString(policyNode).getBytes());
                }
            }
            // 4.update user
            for (String user : node.getUsers()) {
                byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH + user);
                UserNode userNode = JSONObject.parseObject(new String(bytes), UserNode.class);
                if (!userNode.getRoles().contains(path)){
                    userNode.getRoles().add(path);
                    curatorClient.setData().forPath(ZKConstant.ZK_USER_PATH + user,JSONObject.toJSONString(userNode).getBytes());
                }
            }
            String data = JSON.toJSONString(node);
            log.info("===>{} : {}",path,data);
            curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(ZKConstant.ZK_GROUP_PATH+path,data.getBytes());
        }catch (Exception e){
            return e.getMessage();
        }
        return "success";
    }



    @ApiOperation("Update User")
    @PostMapping("/updateUser")
    public String updateUser(@RequestBody @Validated  UserNode userNode){
        String path = userNode.getPath();
        try {
            //1. check group path isExists?
            for (String group : userNode.getGroups()) {
                if (!checkExists(ZKConstant.ZK_GROUP_PATH+group)){
                    log.info("{} not exist！",ZKConstant.ZK_GROUP_PATH + group);
                    return ZKConstant.ZK_GROUP_PATH + group+" not exist！";
                }
            }
            //2. check role path isExists?
            for (String role : userNode.getRoles()) {
                if (!checkExists(ZKConstant.ZK_ROLE_PATH+role)){
                    log.info("{} not exist！",ZKConstant.ZK_ROLE_PATH+role);
                    return ZKConstant.ZK_ROLE_PATH + role+" not exist！";
                }
            }
            // 3. get old data
            byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH + path);
            UserNode old_user = JSONObject.parseObject(new String(bytes), UserNode.class);

            // 4. update group data
            ArrayList<String> old_userGroups = old_user.getGroups();
            List<String> copy_oldGroup = new ArrayList<>(Arrays.asList(new String[old_userGroups.size()]));
            Collections.copy(copy_oldGroup,old_userGroups);
            ArrayList<String> new_userGroups = userNode.getGroups();
            // 4.1 remove new data from old ,need to remove
            copy_oldGroup.removeAll(new_userGroups);
            for (String oldGroup : copy_oldGroup) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_GROUP_PATH + oldGroup);
                GroupNode groupNode = JSONObject.parseObject(new String(bytes1), GroupNode.class);
                groupNode.getUsers().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_GROUP_PATH + oldGroup,JSONObject.toJSONString(groupNode).getBytes());
            }
            // 4.2 remove old data from new ,need to add
            new_userGroups.removeAll(old_userGroups);
            for (String newGroup : new_userGroups) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_GROUP_PATH + newGroup);
                GroupNode groupNode = JSONObject.parseObject(new String(bytes1), GroupNode.class);
                groupNode.getUsers().add(path);
                curatorClient.setData().forPath(ZKConstant.ZK_GROUP_PATH + newGroup,JSONObject.toJSONString(groupNode).getBytes());
            }

            // 5. updata role data
            ArrayList<String> old_userRoles = old_user.getRoles();
            List<String> copy_oldRole = new ArrayList<>(Arrays.asList(new String[old_userRoles.size()]));
            ArrayList<String> new_userRoles = userNode.getRoles();
            Collections.copy(copy_oldRole,old_userRoles);
            // 5.1 remove new data from old ,need to remove
            copy_oldRole.removeAll(new_userRoles);
            for (String oldRole : copy_oldRole) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_ROLE_PATH + oldRole);
                RoleNode roleNode = JSONObject.parseObject(new String(bytes1), RoleNode.class);
                roleNode.getUsers().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_ROLE_PATH + oldRole,JSONObject.toJSONString(roleNode).getBytes());
            }
            // 5.2 remove old data from new ,need to add
            new_userRoles.removeAll(old_userRoles);
            for (String newRole : new_userRoles) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_ROLE_PATH + newRole);
                RoleNode roleNode = JSONObject.parseObject(new String(bytes1), RoleNode.class);
                roleNode.getUsers().add(path);
                curatorClient.setData().forPath(ZKConstant.ZK_ROLE_PATH + newRole,JSONObject.toJSONString(roleNode).getBytes());
            }
            // 6.save new data
            String data = JSON.toJSONString(userNode);
            curatorClient.setData().forPath(ZKConstant.ZK_USER_PATH+path,data.getBytes());
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
        return "success";
    }

    @ApiOperation("Update API")
    @PostMapping("/updateAPI")
    public String updateAPI(@RequestBody @Validated  APINode node){
        String path = node.getPath();
        // 1.check policy path
        if (node.getPolicys() != null){
            for (String policy : node.getPolicys()) {
                if (!checkExists(ZKConstant.ZK_POLICY_PATH + policy)){
                    log.info("{} notexist！",ZKConstant.ZK_POLICY_PATH + policy);
                    return ZKConstant.ZK_POLICY_PATH + policy+" not exist！";
                }
            }
        }
        try {
            // 2. get zk node  old data
            byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_API_PATH +path);
            APINode apiNode = JSONObject.parseObject(new String(bytes1), APINode.class);
            List<String> oldPolicy = apiNode.getPolicys();
            List<String> newPolicy = node.getPolicys();
            List<String> copy_oldPolicy = new ArrayList<>(Arrays.asList(new String[oldPolicy.size()]));
            List<String> copy_newPolicy = new ArrayList<>(Arrays.asList(new String[newPolicy.size()]));
            Collections.copy(copy_oldPolicy,oldPolicy);
            Collections.copy(copy_newPolicy,newPolicy);

            copy_oldPolicy.removeAll(newPolicy);
            //3. remove new data from old data :  need remove
            for (String oldPolicy1 : copy_oldPolicy) {
                byte[] bytes2 = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH + oldPolicy1);
                PolicyNode policyNode1 = JSONObject.parseObject(new String(bytes2), PolicyNode.class);
                ArrayList<String> apis = policyNode1.getApis();
                apis.remove(node.getPath());
                curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH + oldPolicy1,JSONObject.toJSONString(policyNode1).getBytes());
            }
            //4. remove old data from new data :  need add
            copy_newPolicy.removeAll(oldPolicy);
            for (String newPolicy1 : copy_newPolicy) {
                byte[] bytes2 = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH + newPolicy1);
                PolicyNode policyNode1 = JSONObject.parseObject(new String(bytes2), PolicyNode.class);
                ArrayList<String> apis = policyNode1.getApis();
                apis.add(node.getPath());
                curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH + newPolicy1,JSONObject.toJSONString(policyNode1).getBytes());
            }

            String data = JSON.toJSONString(node);
            log.info("updateAPI : {} = {}",ZKConstant.ZK_API_PATH+path,data);
            // 5. save new data
            curatorClient.setData().forPath(ZKConstant.ZK_API_PATH+path,data.getBytes());
            return "success";
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @ApiOperation("Update Policy")
    @PostMapping("/updatePolicy")
    public String updatePolicy(@RequestBody @Validated  PolicyNode policyNode){
        String path = policyNode.getPath();
        try {
            // 1.check groups is exists
            for (String group : policyNode.getGroups()) {
                if (!checkExists(ZKConstant.ZK_GROUP_PATH+group)){
                    log.info("{} not exist！",ZKConstant.ZK_GROUP_PATH + group);
                    return ZKConstant.ZK_GROUP_PATH + group+" not exist！";
                }
            }
            // 2.check roles is exists
            for (String role : policyNode.getRoles()) {
                if (!checkExists(ZKConstant.ZK_ROLE_PATH+role)){
                    log.info("{} not exist！",ZKConstant.ZK_ROLE_PATH+role);
                    return ZKConstant.ZK_ROLE_PATH + role+" not exist！";
                }
            }
            // 3.check apis is exists
            for (String api : policyNode.getApis()) {
                if (!checkExists(ZKConstant.ZK_API_PATH + api)){
                    log.info("{} notexist！",ZKConstant.ZK_API_PATH + api);
                    return ZKConstant.ZK_API_PATH + api+" not exist！";
                }
            }
            // 4. get old data
            byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH+path);
            PolicyNode oldPolicyData = JSONObject.parseObject(new String(bytes), PolicyNode.class);

            // 5. update api
            ArrayList<String> oldApi = oldPolicyData.getApis();
            ArrayList<String> newApi = policyNode.getApis();
            ArrayList<String> copy_oldApi = new ArrayList<>(Arrays.asList(new String[oldApi.size()]));
            ArrayList<String> copy_newApi = new ArrayList<>(Arrays.asList(new String[newApi.size()]));
            Collections.copy(copy_oldApi,oldApi);
            Collections.copy(copy_newApi,newApi);
            // 5.1 remove new from old , need to del this policy
            copy_oldApi.removeAll(newApi);
            for (String copy_old : copy_oldApi) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_API_PATH + copy_old);
                APINode apiNode = JSONObject.parseObject(new String(bytes1), APINode.class);
                apiNode.getPolicys().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_API_PATH + copy_old,JSONObject.toJSONString(apiNode).getBytes());
            }
            // 5.2 remove old from new  ,  need to add this policy
            copy_newApi.removeAll(oldApi);
            for (String copy_new : copy_newApi) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_API_PATH + copy_new);
                APINode apiNode = JSONObject.parseObject(new String(bytes1), APINode.class);
                apiNode.getPolicys().add(path);
                curatorClient.setData().forPath(ZKConstant.ZK_API_PATH + copy_new,JSONObject.toJSONString(apiNode).getBytes());
            }
            // 6. update group
            ArrayList<String> oldGroup = oldPolicyData.getGroups();
            ArrayList<String> newGroup = policyNode.getGroups();
            ArrayList<String> copy_oldGroup = new ArrayList<>(Arrays.asList(new String[oldGroup.size()]));
            ArrayList<String> copy_newGroup = new ArrayList<>(Arrays.asList(new String[newGroup.size()]));
            Collections.copy(copy_oldGroup,oldGroup);
            Collections.copy(copy_newGroup,newGroup);
            // 6.1 remove new from old , need to del this policy
            copy_oldGroup.removeAll(newApi);
            for (String s : copy_oldGroup) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_GROUP_PATH + s);
                GroupNode groupNode = JSONObject.parseObject(new String(bytes1), GroupNode.class);
                groupNode.getPolicys().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_GROUP_PATH + s,JSONObject.toJSONString(groupNode).getBytes());
            }
            // 6.2 remove old from new  ,  need to add this policy
            copy_newGroup.removeAll(oldApi);
            for (String s : copy_newGroup) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_GROUP_PATH + s);
                GroupNode groupNode = JSONObject.parseObject(new String(bytes1), GroupNode.class);
                groupNode.getPolicys().add(path);
                curatorClient.setData().forPath(ZKConstant.ZK_GROUP_PATH + s,JSONObject.toJSONString(groupNode).getBytes());
            }
            // 7. update role
            ArrayList<String> oldRole = oldPolicyData.getRoles();
            ArrayList<String> newRole = policyNode.getRoles();
            ArrayList<String> copy_oldRole = new ArrayList<>(Arrays.asList(new String[oldRole.size()]));
            ArrayList<String> copy_newRole = new ArrayList<>(Arrays.asList(new String[newRole.size()]));
            Collections.copy(copy_oldRole,oldRole);
            Collections.copy(copy_newRole,newRole);
            // 7.1 remove new from old , need to del this policy
            copy_oldRole.removeAll(newRole);
            for (String s : copy_oldRole) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_ROLE_PATH + s);
                RoleNode roleNode = JSONObject.parseObject(new String(bytes1), RoleNode.class);
                roleNode.getPolicys().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_ROLE_PATH + s,JSONObject.toJSONString(roleNode).getBytes());
            }
            // 7.2 remove old from new  ,  need to add this policy
            copy_newRole.removeAll(newRole);
            for (String s : copy_newRole) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_ROLE_PATH + s);
                RoleNode roleNode = JSONObject.parseObject(new String(bytes1), RoleNode.class);
                roleNode.getPolicys().add(path);
                curatorClient.setData().forPath(ZKConstant.ZK_ROLE_PATH + s,JSONObject.toJSONString(roleNode).getBytes());
            }


            String data = JSON.toJSONString(policyNode);
            curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH+path,data.getBytes());
            return "success";
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @ApiOperation("Update Role")
    @PostMapping("/updateRole")
    public String updateRole(@RequestBody @Validated  RoleNode node){
        String path = node.getPath();
        try {
            // 1. check policy path
            for (String policy : node.getPolicys()) {
                if (!checkExists(ZKConstant.ZK_POLICY_PATH + policy)){
                    log.info("{} notexist！",ZKConstant.ZK_POLICY_PATH + policy);
                    return ZKConstant.ZK_POLICY_PATH + policy+" not exist！";
                }
            }
            // 2. check user path
            for (String user : node.getUsers()) {
                if (!checkExists(ZKConstant.ZK_USER_PATH + user)){
                    log.info("{} notexist！",ZKConstant.ZK_USER_PATH + user);
                    return ZKConstant.ZK_USER_PATH + user+" not exist！";
                }
            }

            // 3. get old data
            byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_ROLE_PATH+path);
            RoleNode roleNode = JSONObject.parseObject(new String(bytes), RoleNode.class);
            // 4. update policy
            List<String> old_RolePolicys = roleNode.getPolicys();
            List<String> new_RolePolicys = node.getPolicys();
            ArrayList<String> copy_oldRolePolicys = new ArrayList<>(Arrays.asList(new String[old_RolePolicys.size()]));
            ArrayList<String> copy_newRolePolicys = new ArrayList<>(Arrays.asList(new String[new_RolePolicys.size()]));
            Collections.copy(copy_oldRolePolicys,old_RolePolicys);
            Collections.copy(copy_newRolePolicys,new_RolePolicys);

            // 4.1 remove new data from old data ,need to remove
            copy_oldRolePolicys.removeAll(new_RolePolicys);
            for (String copy_oldRolePolicy : copy_oldRolePolicys) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH+copy_oldRolePolicy);
                PolicyNode policyNode = JSONObject.parseObject(new String(bytes1), PolicyNode.class);
                policyNode.getRoles().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH+copy_oldRolePolicy,JSONObject.toJSONString(policyNode).getBytes());
            }
            // 4.1 remove old data from new data ,need to add
            copy_newRolePolicys.removeAll(old_RolePolicys);
            for (String copy_newRolePolicy : copy_newRolePolicys) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH+copy_newRolePolicy);
                PolicyNode policyNode = JSONObject.parseObject(new String(bytes1), PolicyNode.class);
                policyNode.getRoles().add(path);
                curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH+copy_newRolePolicy,JSONObject.toJSONString(policyNode).getBytes());
            }
            // 5. update user
            List<String> old_users = roleNode.getUsers();
            List<String> new_users = node.getUsers();
            ArrayList<String> copy_old_users = new ArrayList<>(Arrays.asList(new String[old_users.size()]));
            ArrayList<String> copy_new_users = new ArrayList<>(Arrays.asList(new String[new_users.size()]));
            Collections.copy(copy_old_users,old_users);
            Collections.copy(copy_new_users,new_users);

            // 5.1 remove new data from old data ,need to remove
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
            String data = JSON.toJSONString(node);
            curatorClient.setData().forPath(ZKConstant.ZK_ROLE_PATH+path,data.getBytes());
            return "success";
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }

    }
    @PostMapping("/updateGroup")
    public String updateGroup(@RequestBody @Validated  GroupNode node){
        String path = node.getPath();
        try {
            for (String policy : node.getPolicys()) {
                if (!checkExists(ZKConstant.ZK_POLICY_PATH + policy)){
                    log.info("{} notexist！",ZKConstant.ZK_POLICY_PATH + policy);
                    return ZKConstant.ZK_POLICY_PATH + policy+" not exist！";
                }
            }
            // 2. check user path
            for (String user : node.getUsers()) {
                if (!checkExists(ZKConstant.ZK_USER_PATH + user)){
                    log.info("{} notexist！",ZKConstant.ZK_USER_PATH + user);
                    return ZKConstant.ZK_USER_PATH + user+" not exist！";
                }
            }
            // 3. get old data
            byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_GROUP_PATH+path);
            GroupNode groupNode = JSONObject.parseObject(new String(bytes), GroupNode.class);

            // 4. update policy data
            List<String> old_RolePolicys = groupNode.getPolicys();
            List<String> new_RolePolicys = node.getPolicys();
            ArrayList<String> copy_oldRolePolicys = new ArrayList<>(Arrays.asList(new String[old_RolePolicys.size()]));
            ArrayList<String> copy_newRolePolicys = new ArrayList<>(Arrays.asList(new String[new_RolePolicys.size()]));
            Collections.copy(copy_oldRolePolicys,old_RolePolicys);
            Collections.copy(copy_newRolePolicys,new_RolePolicys);

            // 4.1 remove new data from old data ,need to remove
            copy_oldRolePolicys.removeAll(new_RolePolicys);
            for (String copy_oldRolePolicy : copy_oldRolePolicys) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH+copy_oldRolePolicy);
                PolicyNode policyNode = JSONObject.parseObject(new String(bytes1), PolicyNode.class);
                policyNode.getGroups().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH+copy_oldRolePolicy,JSONObject.toJSONString(policyNode).getBytes());
            }
            // 4.1 remove old data from new data ,need to add
            copy_newRolePolicys.removeAll(old_RolePolicys);
            for (String copy_newRolePolicy : copy_newRolePolicys) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH+copy_newRolePolicy);
                PolicyNode policyNode = JSONObject.parseObject(new String(bytes1), PolicyNode.class);
                policyNode.getGroups().add(path);
                curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH+copy_newRolePolicy,JSONObject.toJSONString(policyNode).getBytes());
            }
            // 5. update user
            List<String> old_users = groupNode.getUsers();
            List<String> new_users = node.getUsers();
            ArrayList<String> copy_old_users = new ArrayList<>(Arrays.asList(new String[old_users.size()]));
            ArrayList<String> copy_new_users = new ArrayList<>(Arrays.asList(new String[new_users.size()]));
            Collections.copy(copy_old_users,old_users);
            Collections.copy(copy_new_users,new_users);

            // 5.1 remove new data from old data ,need to remove
            copy_old_users.remove(new_users);
            for (String copy_old_user : copy_old_users) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH+copy_old_user);
                UserNode userNode = JSONObject.parseObject(new String(bytes1), UserNode.class);
                userNode.getGroups().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_USER_PATH+copy_old_user,JSONObject.toJSONString(userNode).getBytes());
            }
            // 5.2 remove old data from new data ,need to add
            copy_new_users.remove(old_users);
            for (String copy_new_user : copy_new_users) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH+copy_new_user);
                UserNode userNode = JSONObject.parseObject(new String(bytes1), UserNode.class);
                userNode.getGroups().add(path);
                curatorClient.setData().forPath(ZKConstant.ZK_USER_PATH+copy_new_user,JSONObject.toJSONString(userNode).getBytes());
            }
            String data = JSON.toJSONString(node);

            curatorClient.setData().forPath(ZKConstant.ZK_GROUP_PATH+path,data.getBytes());
            return "success";
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
    }



    @ApiOperation("Delete User")
    @DeleteMapping("/deleteUser")
    public String deleteUser(@RequestBody ZkNode node){
        String path = node.getPath();
        try {
            // 1. get old data
            byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH+path);
            UserNode userNode = JSONObject.parseObject(new String(bytes1), UserNode.class);
            // 2. remove this user from group date
            ArrayList<String> userNodeGroups = userNode.getGroups();
            for (String userNodeGroup : userNodeGroups) {
                byte[] bytes2 = curatorClient.getData().forPath(ZKConstant.ZK_GROUP_PATH+userNodeGroup);
                GroupNode groupNode = JSONObject.parseObject(new String(bytes2), GroupNode.class);
                groupNode.getUsers().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_GROUP_PATH+userNodeGroup,JSONObject.toJSONString(groupNode).getBytes());
            }
            // 3. remove this user from role date
            ArrayList<String> userNodeRoles = userNode.getRoles();
            for (String userNodeRole : userNodeRoles) {
                byte[] bytes2 = curatorClient.getData().forPath(ZKConstant.ZK_ROLE_PATH+userNodeRole);
                RoleNode roleNode = JSONObject.parseObject(new String(bytes2), RoleNode.class);
                roleNode.getUsers().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_ROLE_PATH+userNodeRole,JSONObject.toJSONString(roleNode).getBytes());
            }
            // 4. delete this user path
            curatorClient.delete().forPath(ZKConstant.ZK_USER_PATH+path);
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
        return "success";
    }

    @ApiOperation("Delete Policy")
    @DeleteMapping("/deletePolicy")
    public String deletePolicy(@RequestBody ZkNode node){
        String path = node.getPath();
        try {
            // 1. get old data
            byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH + path);
            PolicyNode policyNode = JSONObject.parseObject(new String(bytes), PolicyNode.class);

            // 2. remove this policy from apis
            for (String api : policyNode.getApis()) {
                byte[] bytes2 = curatorClient.getData().forPath(ZKConstant.ZK_API_PATH + api);
                APINode apiNode = JSONObject.parseObject(new String(bytes2), APINode.class);
                apiNode.getPolicys().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_API_PATH + api,JSONObject.toJSONString(apiNode).getBytes());
            }
            // 3. remove this policy from group
            for (String group : policyNode.getGroups()) {
                byte[] bytes2 = curatorClient.getData().forPath(ZKConstant.ZK_GROUP_PATH + group);
                GroupNode groupNode = JSONObject.parseObject(new String(bytes2), GroupNode.class);
                groupNode.getPolicys().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_GROUP_PATH + group,JSONObject.toJSONString(groupNode).getBytes());
            }
            // 4. remove this policy from role
            for (String role : policyNode.getRoles()) {
                byte[] bytes2 = curatorClient.getData().forPath(ZKConstant.ZK_ROLE_PATH + role);
                RoleNode roleNode = JSONObject.parseObject(new String(bytes2), RoleNode.class);
                roleNode.getPolicys().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_ROLE_PATH + role,JSONObject.toJSONString(roleNode).getBytes());
            }
            // 5. delete this policy path
            curatorClient.delete().forPath(ZKConstant.ZK_POLICY_PATH+node.getPath());
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
        return "success";
    }
    @ApiOperation("Delete API")
    @DeleteMapping("/deleteAPI")
    public String deleteAPI(@RequestBody ZkNode node){
        try {
            String path = node.getPath();
            // 1. get old data
            byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_API_PATH + node.getPath());
            APINode apiNode = JSONObject.parseObject(new String(bytes1), APINode.class);

            // 2. remove api from policys
            for (String policy : apiNode.getPolicys()) {
                byte[] bytes2 = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH + policy);
                PolicyNode policyNode = JSONObject.parseObject(new String(bytes2), PolicyNode.class);
                policyNode.getApis().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH + policy,JSONObject.toJSONString(policyNode).getBytes());
            }
            // 3. delete this api path
            curatorClient.delete().forPath(ZKConstant.ZK_API_PATH+path);
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
        return "success";
    }

    @ApiOperation("Delete Group")
    @DeleteMapping("/deleteGroup")
    public String deleteGroup(@RequestBody ZkNode node){
        try {
            String path = node.getPath();
            // 1. get old data
            byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_GROUP_PATH + path);
            GroupNode groupNode = JSONObject.parseObject(new String(bytes), GroupNode.class);
            // 2. remove group from policys
            for (String policy : groupNode.getPolicys()) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH + policy);
                PolicyNode policyNode = JSONObject.parseObject(new String(bytes1), PolicyNode.class);
                policyNode.getGroups().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH + policy,JSONObject.toJSONString(policyNode).getBytes());
            }

            // 3. remove group from users
            for (String user : groupNode.getUsers()) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH + user);
                UserNode userNode = JSONObject.parseObject(new String(bytes1), UserNode.class);
                userNode.getGroups().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_USER_PATH + user,JSONObject.toJSONString(userNode).getBytes());
            }
            // 3. delete this  group path
            curatorClient.delete().forPath(ZKConstant.ZK_GROUP_PATH+path);
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
        return "success";
    }
    @ApiOperation("Delete Role")
    @DeleteMapping("/deleteRole")
    public String deleteRole(@RequestBody ZkNode node){
        try {
            String path = node.getPath();
            // 1. get old data
            byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_ROLE_PATH + path);
            RoleNode roleNode = JSONObject.parseObject(new String(bytes), RoleNode.class);
            // 2. remove role from policys
            for (String policy : roleNode.getPolicys()) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH + policy);
                PolicyNode policyNode = JSONObject.parseObject(new String(bytes1), PolicyNode.class);
                policyNode.getRoles().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH + policy,JSONObject.toJSONString(policyNode).getBytes());
            }

            // 3. remove role from users
            for (String user : roleNode.getUsers()) {
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH + user);
                UserNode userNode = JSONObject.parseObject(new String(bytes1), UserNode.class);
                userNode.getRoles().remove(path);
                curatorClient.setData().forPath(ZKConstant.ZK_USER_PATH + user,JSONObject.toJSONString(userNode).getBytes());
            }
            // 3. delete this  role path
            curatorClient.delete().forPath(ZKConstant.ZK_ROLE_PATH+path);
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
        return "success";
    }











    @ApiOperation("Select Group Detail Info")
    @GetMapping("/select/group/{groupId}")
    public HashMap selectGroup(@PathVariable("groupId") String groupId) throws Exception {
        HashMap<String, Object> result = new HashMap<>();
        byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_GROUP_PATH + "/" + groupId);
        GroupNode groupNode = JSONObject.parseObject(new String(bytes), GroupNode.class);
        result.put("groupId",groupNode.getPath());
        result.put("policys",groupNode.getPolicys());
        result.put("users",groupNode.getUsers());
        return result;

    }

    @ApiOperation("Select Role Detail Info")
    @GetMapping("/select/role/{roleId}")
    public HashMap selectRole(@PathVariable("roleId") String roleId) throws Exception {
        HashMap<String, Object> result = new HashMap<>();
        byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_ROLE_PATH + "/" + roleId);
        RoleNode roleNode = JSONObject.parseObject(new String(bytes), RoleNode.class);
        result.put("roleId",roleNode.getPath());
        result.put("policys",roleNode.getPolicys());
        result.put("users",roleNode.getUsers());
        return result;

    }
    @ApiOperation("Select User Detail Info")
    @GetMapping("/select/user/{userId}")
    public HashMap selectUser(@PathVariable("userId") String userId) throws Exception {
        HashMap<String, Object> result = new HashMap<>();
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
        return result;
    }


    @ApiOperation("Get ZK Node Info")
    @PostMapping("/getData")
    public String getData(@RequestBody ZkNode node) throws Exception {
        String path = node.getPath();
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
        return new String(bytes);
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
    @ApiOperation("Get Policy Node All Permission")
    @GetMapping("/getPolicyPermission/{path}")
    public String getPolicyPermission(@PathVariable("path")String path) throws Exception {
        HashSet<String> ans = new HashSet<>();
        findPolicyPermission(ZKConstant.ZK_POLICY_PATH+"/"+path,ans);
        return ans.toString();
    }

    public void findPolicyPermission(String path ,HashSet<String> ans) throws Exception{

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
    }


}

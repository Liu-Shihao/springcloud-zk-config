package com.lsh.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lsh.auth.dto.ZkNode;
import com.lsh.auth.dto.zk.PolicyNode;
import com.lsh.auth.dto.zk.UserNode;
import com.lsh.constant.ZKConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @Author: LiuShihao
 * @Date: 2022/9/8 10:36
 * @Desc:
 */

@Slf4j
@RestController
@RequestMapping("/curator")
public class CuratorController {

    @Autowired
    CuratorFramework curatorClient;

    /**
     * create zk node
     * node type : api/policy/group/role/user
     * api:
     * 	["/policy1","/policy2","/policy3"]
     *
     * policy:
     *    {
     * 	"apis":["/api1","api3"],
     * 	"roles":["/role1","/role3"],
     * 	"groups":["/group1","/group3"],
     * 	"ancestors":["/policy1","policy3"],
     * 	"childrens":["/policy7","/policy8"]
     *    }
     *
     * group:
     * 	["/policy1","/policy3","/policy5"]
     *
     * role:
     * 	["/policy2","/policy4","/policy6"]
     *
     * user:
     *    {
     * 	"groups":["/group1","/group2"],
     * 	"roles":["/role3","/role4"]
     *    }
     * @param node
     * @return
     * @throws Exception
     */
    @PostMapping("/createNode")
    public String createNode(@RequestBody @Validated ZkNode node) throws Exception {
        String path = node.getPath();
        String data ;
        switch (node.getType()){
            case "user":
                if (!path.startsWith(ZKConstant.ZK_USER_PATH)){
                    path = ZKConstant.ZK_USER_PATH+path;
                }
                UserNode userNode = node.getUserNode();
                //1. check path isExists?
                for (String group : userNode.getGroups()) {
                    if (!checkExists(ZKConstant.ZK_GROUP_PATH+group)){
                        log.info("{} not exist！",ZKConstant.ZK_GROUP_PATH + group);
                        return ZKConstant.ZK_GROUP_PATH + group+" not exist！";
                    }
                }
                for (String role : userNode.getRoles()) {
                    if (!checkExists(ZKConstant.ZK_ROLE_PATH+role)){
                        log.info("{} not exist！",ZKConstant.ZK_ROLE_PATH+role);
                        return ZKConstant.ZK_ROLE_PATH + role+" not exist！";
                    }
                }
                data = JSON.toJSONString(userNode);
                break;
            case "policy":
                //create policy node
                if (!path.startsWith(ZKConstant.ZK_POLICY_PATH)){
                    path = ZKConstant.ZK_POLICY_PATH+path;
                }
                PolicyNode policyNode =  node.getPolicyNode();
                //first check api is exist？
                for (String api : policyNode.getApis()) {
                    Stat stat = curatorClient.checkExists().forPath(ZKConstant.ZK_API_PATH + api);
                    if (stat == null){
                        log.info("{} not exist！",ZKConstant.ZK_API_PATH + api);
                        return ZKConstant.ZK_API_PATH + api+" not exist！";
                    }
                }
                //second update api node data
                for (String api : policyNode.getApis()) {
                    byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_API_PATH + api);
                    List<String> arrayLists = JSONObject.parseArray(new String(bytes), String.class);
                    arrayLists.add(node.getPath());
                    curatorClient.setData().forPath(ZKConstant.ZK_API_PATH + api,JSONObject.toJSONString(arrayLists).getBytes());
                }
                data = JSON.toJSONString(policyNode);
                break;
            case "group":
                if (!path.startsWith(ZKConstant.ZK_GROUP_PATH)){
                    path = ZKConstant.ZK_GROUP_PATH+path;
                }
                if (node.getPolicys() != null){
                    for (String policy : node.getPolicys()) {
                        if (!checkExists(ZKConstant.ZK_POLICY_PATH + policy)){
                            log.info("{} notexist！",ZKConstant.ZK_POLICY_PATH + policy);
                            return ZKConstant.ZK_POLICY_PATH + policy+" not exist！";
                        }
                    }
                }
                data = JSON.toJSONString(node.getPolicys());
                break;
            case "role":
                if (!path.startsWith(ZKConstant.ZK_ROLE_PATH)){
                    path = ZKConstant.ZK_ROLE_PATH+path;
                }
                if (node.getPolicys() != null){
                    for (String policy : node.getPolicys()) {
                        if (!checkExists(ZKConstant.ZK_POLICY_PATH + policy)){
                            log.info("{} notexist！",ZKConstant.ZK_POLICY_PATH + policy);
                            return ZKConstant.ZK_POLICY_PATH + policy+" not exist！";
                        }
                    }
                }
                data = JSON.toJSONString(node.getPolicys());
                break;
            case "api":
                if (!path.startsWith(ZKConstant.ZK_API_PATH)){
                    path = ZKConstant.ZK_API_PATH+path;
                }
                //create api node,update policy node
                ArrayList<String> noexists = new ArrayList<>();
                for (String policy : node.getPolicys()) {
                    if (checkExists(ZKConstant.ZK_POLICY_PATH+policy)){
                        byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH+policy);
                        PolicyNode parseObject = JSONObject.parseObject(new String(bytes), PolicyNode.class);
                        parseObject.getApis().add(node.getPath());
                        curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH+policy,JSONObject.toJSONBytes(parseObject));
                    }else {
                        log.error("create api node ：{} path not exist！",ZKConstant.ZK_POLICY_PATH+policy);
                        noexists.add(policy);
                    }
                }
                node.getPolicys().removeAll(noexists);
                data = JSON.toJSONString(node.getPolicys());
                break;
            default:
                return "node type illegal";
        }
        log.info("===>{} : {}",path,data);
        try {
            curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path,data.getBytes());
        }catch (KeeperException.NodeExistsException e){
            return e.getMessage();
        }
        return "success";
    }


    @PostMapping("/updateNode")
    public String updateNode(@RequestBody @Validated  ZkNode node) throws Exception {
        String path = node.getPath();
        String data ;
        switch (node.getType()){
            case "user":
                if (!path.startsWith(ZKConstant.ZK_USER_PATH)){
                    path = ZKConstant.ZK_USER_PATH+path;
                }
                UserNode userNode = node.getUserNode();
                //check path isExists?
                for (String group : userNode.getGroups()) {
                    if (!checkExists(ZKConstant.ZK_GROUP_PATH+group)){
                        log.info("{} not exist！",ZKConstant.ZK_GROUP_PATH + group);
                        return ZKConstant.ZK_GROUP_PATH + group+" not exist！";
                    }
                }
                for (String role : userNode.getRoles()) {
                    if (!checkExists(ZKConstant.ZK_ROLE_PATH+role)){
                        log.info("{} not exist！",ZKConstant.ZK_ROLE_PATH+role);
                        return ZKConstant.ZK_ROLE_PATH + role+" not exist！";
                    }
                }
                data = JSON.toJSONString(userNode);
                break;
            case "policy":
                //create policy node
                PolicyNode policyNode =  node.getPolicyNode();
                if (!path.startsWith(ZKConstant.ZK_POLICY_PATH)){
                    path = ZKConstant.ZK_POLICY_PATH+path;
                }
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
                // 4. update api node date
                byte[] bytes = curatorClient.getData().forPath(path);
                PolicyNode oldPolicyNodeData = JSONObject.parseObject(new String(bytes), PolicyNode.class);
                ArrayList<String> oldApis = oldPolicyNodeData.getApis();
                ArrayList<String> newAps = policyNode.getApis();
                ArrayList<String> copy_old = new ArrayList<>(Arrays.asList(new String[oldApis.size()]));
                ArrayList<String> copy_new = new ArrayList<>(Arrays.asList(new String[newAps.size()]));
                Collections.copy(copy_old,oldApis);
                Collections.copy(copy_new,newAps);
                // from old remove new , old remaining api node need to del this policy
                copy_old.removeAll(newAps);
                for (String oldApi : copy_old) {
                    byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_API_PATH + oldApi);
                    List<String> array = JSONObject.parseArray(new String(bytes1), String.class);
                    array.remove(node.getPath());
                    curatorClient.setData().forPath(ZKConstant.ZK_API_PATH + oldApi,JSONObject.toJSONString(array).getBytes());
                }
                // from new remove old , new remaining api node need to add this policy
                copy_new.removeAll(oldApis);
                for (String newApi : copy_new) {
                    byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_API_PATH + newApi);
                    List<String> array = JSONObject.parseArray(new String(bytes1), String.class);
                    array.add(node.getPath());
                    curatorClient.setData().forPath(ZKConstant.ZK_API_PATH + newApi,JSONObject.toJSONString(array).getBytes());
                }
                data = JSON.toJSONString(policyNode);
                break;
            case "group":
                if (!path.startsWith(ZKConstant.ZK_GROUP_PATH)){
                    path = ZKConstant.ZK_GROUP_PATH+path;
                }
                if (node.getPolicys() != null){
                    for (String policy : node.getPolicys()) {
                        if (!checkExists(ZKConstant.ZK_POLICY_PATH + policy)){
                            log.info("{} notexist！",ZKConstant.ZK_POLICY_PATH + policy);
                            return ZKConstant.ZK_POLICY_PATH + policy+" not exist！";
                        }
                    }
                }
                data = JSON.toJSONString(node.getPolicys());
                break;
            case "role":
                if (!path.startsWith(ZKConstant.ZK_ROLE_PATH)){
                    path = ZKConstant.ZK_ROLE_PATH+path;
                }
                if (node.getPolicys() != null){
                    for (String policy : node.getPolicys()) {
                        if (!checkExists(ZKConstant.ZK_POLICY_PATH + policy)){
                            log.info("{} notexist！",ZKConstant.ZK_POLICY_PATH + policy);
                            return ZKConstant.ZK_POLICY_PATH + policy+" not exist！";
                        }
                    }
                }
                data = JSON.toJSONString(node.getPolicys());
                break;
            case "api":
                if (!path.startsWith(ZKConstant.ZK_API_PATH)){
                    path = ZKConstant.ZK_API_PATH+path;
                }
                if (node.getPolicys() != null){
                    for (String policy : node.getPolicys()) {
                        if (!checkExists(ZKConstant.ZK_POLICY_PATH + policy)){
                            log.info("{} notexist！",ZKConstant.ZK_POLICY_PATH + policy);
                            return ZKConstant.ZK_POLICY_PATH + policy+" not exist！";
                        }
                    }
                }
                //update policy node data
                byte[] bytes1 = curatorClient.getData().forPath(path);
                List<String> oldPolicy = JSONObject.parseArray(new String(bytes1), String.class);
                List<String> newPolicy = node.getPolicys();
                List<String> copy_oldPolicy = new ArrayList<>(Arrays.asList(new String[oldPolicy.size()]));
                List<String> copy_newPolicy = new ArrayList<>(Arrays.asList(new String[newPolicy.size()]));
                log.info("------------oldPolicy size :{}, copy_oldPolicy size :{}",oldPolicy.size(),copy_oldPolicy.size());
                Collections.copy(copy_oldPolicy,oldPolicy);
                Collections.copy(copy_newPolicy,newPolicy);
                copy_oldPolicy.removeAll(newPolicy);
                //remove api from policy
                for (String oldPolicy1 : copy_oldPolicy) {
                    byte[] bytes2 = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH + oldPolicy1);
                    PolicyNode policyNode1 = JSONObject.parseObject(new String(bytes2), PolicyNode.class);
                    ArrayList<String> apis = policyNode1.getApis();
                    apis.remove(node.getPath());
                    curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH + oldPolicy1,JSONObject.toJSONString(policyNode1).getBytes());
                }
                //add api from policy
                copy_newPolicy.removeAll(oldPolicy);
                for (String newPolicy1 : copy_newPolicy) {
                    byte[] bytes2 = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH + newPolicy1);
                    PolicyNode policyNode1 = JSONObject.parseObject(new String(bytes2), PolicyNode.class);
                    ArrayList<String> apis = policyNode1.getApis();
                    apis.add(node.getPath());
                    curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH + newPolicy1,JSONObject.toJSONString(policyNode1).getBytes());
                }
                data = JSON.toJSONString(node.getPolicys());
                break;
            default:
                return "node type illegal";
        }

        curatorClient.setData().forPath(path,data.getBytes());
        return "success";
    }


    @PostMapping("/deleteNode")
    public String deleteNode(@RequestBody ZkNode node) throws Exception {
        switch (node.getType()){
            case "user":
                curatorClient.delete().forPath(ZKConstant.ZK_USER_PATH+node.getPath());
                break;
            case "policy":
                // del policy need to update api
                byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH + node.getPath());
                PolicyNode policyNode = JSONObject.parseObject(new String(bytes), PolicyNode.class);
                for (String api : policyNode.getApis()) {
                    byte[] bytes2 = curatorClient.getData().forPath(ZKConstant.ZK_API_PATH + api);
                    List<String> list = JSONObject.parseArray(new String(bytes2), String.class);
                    list.remove(node.getPath());
                    curatorClient.setData().forPath(ZKConstant.ZK_API_PATH + api,JSONObject.toJSONString(list).getBytes());
                }

                curatorClient.delete().forPath(ZKConstant.ZK_POLICY_PATH+node.getPath());
                break;
            case "group":
                curatorClient.delete().forPath(ZKConstant.ZK_GROUP_PATH+node.getPath());
                break;
            case "role":
                curatorClient.delete().forPath(ZKConstant.ZK_ROLE_PATH+node.getPath());
                break;
            case "api":
                // del api need to update policy
                byte[] bytes1 = curatorClient.getData().forPath(ZKConstant.ZK_API_PATH + node.getPath());
                List<String> policys = JSONObject.parseArray(new String(bytes1), String.class);
                for (String policy : policys) {
                    byte[] bytes3 = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH + policy);
                    PolicyNode policyNode1 = JSONObject.parseObject(new String(bytes3), PolicyNode.class);
                    policyNode1.getApis().remove(node.getPath());
                    curatorClient.setData().forPath(ZKConstant.ZK_POLICY_PATH + policy,JSONObject.toJSONString(policyNode1).getBytes());
                }
                curatorClient.delete().forPath(ZKConstant.ZK_API_PATH+node.getPath());
                break;
            default:
                break;
        }
        return "success";
    }



    @PostMapping("/select/group/{groupId}")
    public HashMap selectGroup(@PathVariable("groupId") String groupId) throws Exception {
        HashMap<String, Object> result = new HashMap<>();
        byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_GROUP_PATH + "/" + groupId);
        List<String> policys = JSONObject.parseArray(new String(bytes), String.class);
        List<String> users = curatorClient.getChildren().forPath(ZKConstant.ZK_USER_PATH);
        ArrayList<String> groupUsers = new ArrayList<>();
        for (String user : users) {
            String path = ZKConstant.ZK_USER_PATH+"/"+user;
            byte[] bytes1 = curatorClient.getData().forPath(path);
            UserNode userNode = JSON.parseObject(new String(bytes1), UserNode.class);
            if (userNode.getGroups().contains("/"+groupId)){
                groupUsers.add(user);
            }
        }
        result.put("groupId",groupId);
        result.put("policys",policys);
        result.put("users",groupUsers);
        return result;

    }
    @PostMapping("/select/role/{roleId}")
    public HashMap selectRole(@PathVariable("roleId") String roleId) throws Exception {
        HashMap<String, Object> result = new HashMap<>();
        byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_ROLE_PATH + "/" + roleId);
        List<String> policys = JSONObject.parseArray(new String(bytes), String.class);
        List<String> users = curatorClient.getChildren().forPath(ZKConstant.ZK_USER_PATH);
        ArrayList<String> groupUsers = new ArrayList<>();
        for (String user : users) {
            String path = ZKConstant.ZK_USER_PATH+"/"+user;
            byte[] bytes1 = curatorClient.getData().forPath(path);
            UserNode userNode = JSON.parseObject(new String(bytes1), UserNode.class);
            if (userNode.getRoles().contains("/"+roleId)){
                groupUsers.add(user);
            }
        }
        result.put("roleId",roleId);
        result.put("policys",policys);
        result.put("users",groupUsers);
        return result;

    }





    @PostMapping("/select/user/{userId}")
    public HashMap selectUser(@PathVariable("userId") String userId) throws Exception {
        HashMap<String, Object> result = new HashMap<>();
        byte[] bytes = curatorClient.getData().forPath(ZKConstant.ZK_USER_PATH + "/" + userId);
        UserNode userNode = JSONObject.parseObject(new String(bytes), UserNode.class);
        ArrayList<String> groups = userNode.getGroups();
        ArrayList<String> roles = userNode.getRoles();

        HashSet<String> userPolicy = new HashSet<>();

        HashSet<String> userApi = new HashSet<>();

        for (String group : groups) {
            byte[] bytes2 = curatorClient.getData().forPath(ZKConstant.ZK_GROUP_PATH +  group);
            List<String> groupPolicy = JSONObject.parseArray(new String(bytes2), String.class);
            userPolicy.addAll(groupPolicy);
        }
        for (String role : roles) {
            byte[] bytes3 = curatorClient.getData().forPath(ZKConstant.ZK_ROLE_PATH +  role);
            List<String> rolePolicy = JSONObject.parseArray(new String(bytes3), String.class);
            userPolicy.addAll(rolePolicy);
        }

        for (String policy : userPolicy) {
            byte[] bytes3 = curatorClient.getData().forPath(ZKConstant.ZK_POLICY_PATH +  policy);
            PolicyNode policyNode = JSONObject.parseObject(new String(bytes3), PolicyNode.class);
            ArrayList<String> apis = policyNode.getApis();
            userApi.addAll(apis);
        }

        result.put("userId",userId);
        result.put("groups",groups);
        result.put("roles",roles);
        result.put("policys",userPolicy);
        result.put("apis",userApi);
        return result;
    }


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







}

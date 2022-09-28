package com.citi.zk.controller;

import com.citi.zk.dto.zk.*;
import com.citi.zk.service.CuratorService;
import com.citi.zk.dto.ZkNode;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/curator")
public class CuratorController {


    @Autowired
    CuratorService curatorService;

    /**
     * @param userNode
     * @return
     * @throws Exception
     */
    @ApiOperation("Create User")
    @PostMapping("/createUser")
    public String createUser(@RequestBody @Validated @ApiParam("UserNode Info") UserNode userNode)  {
        return curatorService.createUser(userNode);
    }

    @ApiOperation("Create API")
    @PostMapping("/createAPI")
    public String createApi(@RequestBody @Validated @ApiParam("APINode Info") APINode node){
        return curatorService.createApi(node);
    }

    @ApiOperation("Create Policy")
    @PostMapping("/createPolicy")
    public String createPolicy(@RequestBody @Validated @ApiParam("PolicyNode Info") PolicyNode policyNode){
        return curatorService.createPolicy(policyNode);
    }
    @ApiOperation("Create Role")
    @PostMapping("/createRole")
    public String createRole(@RequestBody @Validated RoleNode node){
        return curatorService.createRole(node);
    }

    @ApiOperation("Create Group")
    @PostMapping("/createGroup")
    public String createGroup(@RequestBody @Validated GroupNode node){
        return curatorService.createGroup(node);
    }

    @ApiOperation("Update User")
    @PostMapping("/updateUser")
    public String updateUser(@RequestBody @Validated  UserNode userNode){
        return curatorService.updateUser(userNode);
    }

    @ApiOperation("Update API")
    @PostMapping("/updateAPI")
    public String updateAPI(@RequestBody @Validated  APINode node){
        return curatorService.updateAPI(node);
    }

    @ApiOperation("Update Policy")
    @PostMapping("/updatePolicy")
    public String updatePolicy(@RequestBody @Validated  PolicyNode policyNode){
        return curatorService.updatePolicy(policyNode);
    }

    @ApiOperation("Update Role")
    @PostMapping("/updateRole")
    public String updateRole(@RequestBody @Validated  RoleNode node){
        return curatorService.updateRole(node);
    }
    @PostMapping("/updateGroup")
    public String updateGroup(@RequestBody @Validated  GroupNode node){
        return curatorService.updateGroup(node);
    }

    @ApiOperation("Delete User")
    @DeleteMapping("/deleteUser")
    public String deleteUser(@RequestBody ZkNode node){
        return curatorService.deleteUser(node);
    }

    @ApiOperation("Delete Policy")
    @DeleteMapping("/deletePolicy")
    public String deletePolicy(@RequestBody ZkNode node){
        return curatorService.deletePolicy(node);
    }
    @ApiOperation("Delete API")
    @DeleteMapping("/deleteAPI")
    public String deleteAPI(@RequestBody ZkNode node){
        return curatorService.deleteAPI(node);
    }

    @ApiOperation("Delete Group")
    @DeleteMapping("/deleteGroup")
    public String deleteGroup(@RequestBody ZkNode node){
        return curatorService.deleteGroup(node);
    }
    @ApiOperation("Delete Role")
    @DeleteMapping("/deleteRole")
    public String deleteRole(@RequestBody ZkNode node){
        return curatorService.deleteRole(node);
    }


    @ApiOperation("Select Group Detail Info")
    @GetMapping("/select/group/{groupId}")
    public HashMap selectGroup(@PathVariable("groupId") String groupId) throws Exception {
        return curatorService.selectGroup(groupId);
    }

    @ApiOperation("Select Role Detail Info")
    @GetMapping("/select/role/{roleId}")
    public HashMap selectRole(@PathVariable("roleId") String roleId) throws Exception {
        return curatorService.selectRole(roleId);
    }
    @ApiOperation("Select User Detail Info")
    @GetMapping("/select/user/{userId}")
    public HashMap selectUser(@PathVariable("userId") String userId) throws Exception {
        return curatorService.selectUser(userId);
    }

    @ApiOperation("Get ZK Node Info")
    @PostMapping("/getData")
    public String getData(@RequestBody ZkNode node) throws Exception {
        return curatorService.getData(node);
    }

    @ApiOperation("Get Policy Node All Permission")
    @GetMapping("/getPolicyPermission/{path}")
    public String getPolicyPermission(@PathVariable("path")String path) throws Exception {
        return curatorService.getPolicyPermission(path);
    }

}

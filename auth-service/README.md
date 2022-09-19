# Swagger 配置
http://localhost:8004/swagger-ui.html

```java
/**
 @Api：修饰整个类，描述Controller的作用
 @ApiOperation：描述一个类的一个方法，或者说一个接口
 @ApiParam：单个参数描述
 @ApiModel：用对象来接收参数
 @ApiProperty：用对象接收参数时，描述对象的一个字段
 @ApiResponse：HTTP响应其中1个描述
 @ApiResponses：HTTP响应整体描述
 @ApiIgnore：使用该注解忽略这个API
 @ApiError ：发生错误返回的信息
 @ApiImplicitParam：一个请求参数
 @ApiImplicitParams：多个请求参数
 */


```
# Curator
目前 Curator 有2.x.x和3.x.x两个系列的版本，支持不同版本的Zookeeper。其中Curator 2.x.x兼容Zookeeper的3.4.x和3.5.x。而Curator 3.x.x只兼容Zookeeper 3.5.x，并且提供了一些诸如动态重新配置、watch删除等新特性。

事件监听
zookeeper原生支持通过注册watcher来进行事件监听，但是其使用不是特别方便，需要开发人员自己反复注册watcher，比较繁琐。

Curator引入Cache来实现对zookeeper服务端事务的监听。Cache是Curator中对事件监听的包装，其对事件的监听其实可以近似看作是一个本地缓存视图和远程Zookeeper视图的对比过程。同时，Curator能够自动为开发人员处理反复注册监听，从而大大简化原生api开发的繁琐过程。




user --> role、group  --> policy  <--> api

反向索引表：
policy --> user
group --> user
role --> user

为什么需要维护反向索引表？
因为zk存储的数据，只能从user到role、group、policy,而如果role、group、policy发生变动，需要更新缓存中user的权限，则只能遍历全量user查询发生变动的role、group、policy。

user 更新：--> role、group  --> policy  <--> api
policy更新：（需要查询使用当前policy的所有user），查询反向索引表，更新涉及的user
role、group更新：需要查询所有拥有当前role的user（当前group的所有user），查询反向索引表，更新涉及的user
api更新：不需要更新缓存，因为user的权限是基于policy的，只有policy发生变化时才需要更新缓存


1. 创建user节点：检查是否存在对应的role、group节点，如果不存在，不能创建。
2. 创建role、group节点：检查是否存在对应的policy节点，如果不存在，不能创建。
3. 创建policy节点：检查是否存在对应的api节点，如果不存在，不能创建。
4. 创建api节点：检查policy节点是否存在，如果不存在，从数组中删除此policy数据后创建


1. 更新user节点：检查role、group节点，如果不存在，不能创建。
2. 更新role、group节点：检查policy节点，如果不存在，不能创建。
3. 更新policy节点：检查api节点，如果不存在，不能创建；查询该policy节点的旧api数据，对比新的api数据，在新数据中不存在的旧api数据，需要删除旧api节点中的该policy数据；在旧数据中不存在的新api，需要在新api节点上加上改policy数据。
4. 更新api节点：检查policy节点是否存在，如果不存在，不能创建；同上：查询改api节点旧的policy数据，对比新的policy数据，在新数据中不存在的旧的policy，需要从旧的policy节点数据中删除该api；在旧数据中不存在的新policy数据，则需要在新的policy节点上添加该api数据


1. 删除user节点：直接删除user节点。
2. 删除role、group节点：直接删除。
3. 删除policy节点：首先需要查询节点旧数据，找到关联的api节点，从所有api节点中删除policy数据后，在删除该policy节点。
4. 删除api节点：同上，查询旧数据，找到关联的policy节点，从中删除api数据，再删除该api节点



建议：role、group节点数据维护一份user列表
["/policy2","/policy4","/policy6"]

修改后：

{
"policys":["/policy1","/policy2"],
"users":["/user1","/user2"]

}







#auth-service


1. 连接zk集群
2. guava 建立本地缓存，本地缓存中没查到，会从zk中重新加载
3. zk数据变化，清空本地缓存
4. 提供API鉴权



# gateway-service

1. 定义GlobalFilter，请求auth-service鉴权API

http://localhost:8000/api1?userId=SL001&businessCode=Real-Time-Payments


# zk性能测试
3节点 4核4G 30G



操作     | 10w节点 | 20w节点 | 30w节点
-------- | -----| -----| -----
创建  |  0.0017(3mins) |  0.0018(6mins)| 0.0020(616s)
读  |   0.00057 (57s)|0.00059 (118s)| 0.00063(189s)
写  |   0.00203(203s)|0.00203(406s)| 0.00202（616s）
读写  |  0.00288（288s） |0.00278(540s)| 0.00309s(928s)
删除  |  0.00198 (198s)|0.00198(397s)| 0.00196 (588s) 



zookeeper中已有10w节点，创建一个新节点






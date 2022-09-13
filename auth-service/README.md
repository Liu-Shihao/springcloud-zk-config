api节点:  ["/policy1","/policy2","/policy3"]
policy节点:  "apis":["/api1","api3"],

1. 创建api：如果policy不存在，从数组中删除此policy，并更新对应的policy
2. 创建policy：检查api是否存在，不存在禁止创建；存在则更新相应的api节点数据
更新api：如果policy不存在，直接跳过，更新对应policy节点
更新policy：检查api是否存在，更新相应的api节点数据





1. create api node：don't check policy is exists
2. 







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






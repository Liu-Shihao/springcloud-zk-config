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






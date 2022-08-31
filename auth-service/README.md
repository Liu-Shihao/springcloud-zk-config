#auth-service


1. 连接zk集群
2. guava 建立本地缓存，本地缓存中没查到，会从zk中重新加载
3. zk数据变化，清空本地缓存
4. 提供API鉴权



# gateway-service

1. 定义GlobalFilter，请求auth-service鉴权API
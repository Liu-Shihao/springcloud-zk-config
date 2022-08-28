#auth-service
# 环境

<table>
<tr>
<td>
Hazelcast
</td>
<td>
192.168.153.129:5701
</td>
</tr>

<tr>
<td>
zookeeper
</td>
<td>
192.168.153.128:2181,192.168.153.129:2181,192.168.153.130:2181
</td>
</tr>
</table>

## 启动auth-service服务
1. 连接zk、连接hazelcast
2. 查询db
3. 将db数据写入缓存
4. 更新zk version

#auth-util 
1. 连接zk，监听节点version
2. 连接hazelcast，写入本地缓存
3. 让其他服务引用，interceptor拦截器进行鉴权



#DB
user 

group 

role

policy

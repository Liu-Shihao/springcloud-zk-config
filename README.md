# Zookeeper 做配置中心

利用watch机制监听节点，如果权限发生改变，及时通知各个服务更新本地缓存


# ConsumerService
服务启动后。连接zk节点，然后拉去本地缓存
此时节点不存在，阻塞等待


# 数据库
group

user

role

policy

api
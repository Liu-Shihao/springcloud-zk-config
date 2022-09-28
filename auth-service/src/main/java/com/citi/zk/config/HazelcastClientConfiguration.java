//package com.lsh.auth.config;
//
//import com.hazelcast.client.HazelcastClient;
//import com.hazelcast.client.config.ClientConfig;
//import com.hazelcast.client.config.ClientNetworkConfig;
//import com.hazelcast.core.HazelcastInstance;
//import lombok.Data;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * @Author: LiuShihao
// * @Date: 2022/8/22 23:01
// * @Desc: 连接Hazelcast Cluster集群
// */
//@ConfigurationProperties(prefix = "hazelcast")
//@Data
//@Configuration
//public class HazelcastConfiguration {
//
//    public String address;
//
//    @Bean
//    public ClientConfig hazelCastConfig() {
//        ClientConfig clientConfig = new ClientConfig();
//        ClientNetworkConfig networkConfig = clientConfig.getNetworkConfig();
//        networkConfig.addAddress( address)
//                .setSmartRouting(true)
//                .addOutboundPortDefinition("34700-34710")
//                .setRedoOperation(true)
//                .setConnectionTimeout(5000);
//        return clientConfig;
//
//    }
//
//    /**
//     * @param config
//     * @return
//     */
//    @Bean("hazelcastClient")
//    public HazelcastInstance hazelcastInstance(ClientConfig config) {
//        HazelcastInstance hazelcastInstance = HazelcastClient.newHazelcastClient(config);
//        return hazelcastInstance;
//    }
//
//}

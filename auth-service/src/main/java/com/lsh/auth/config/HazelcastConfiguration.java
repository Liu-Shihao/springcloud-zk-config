package com.lsh.auth.config;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/22 23:01
 * @Desc: 连接Hazelcast Cluster集群
 */
@Configuration
public class HazelcastConfiguration {

    @Bean
    public ClientConfig hazelCastConfig() {
        ClientConfig clientConfig = new ClientConfig();
        ClientNetworkConfig networkConfig = clientConfig.getNetworkConfig();
        networkConfig.addAddress( "192.168.153.129:5701")
                .setSmartRouting(true)
                .addOutboundPortDefinition("34700-34710")
                .setRedoOperation(true)
                .setConnectionTimeout(5000);
        return clientConfig;

    }

    /**
     * @param config
     * @return
     */
    @Bean("hazelcastClient")
    public HazelcastInstance hazelcastInstance(ClientConfig config) {
        HazelcastInstance hazelcastInstance = HazelcastClient.newHazelcastClient(config);
        Map<String, Object> cache = hazelcastInstance.getMap("test");
        System.out.println("======test :"+cache.get("date"));
        return hazelcastInstance;
    }

}

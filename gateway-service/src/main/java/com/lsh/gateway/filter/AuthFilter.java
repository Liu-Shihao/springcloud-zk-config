package com.lsh.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/31 12:41
 * @Desc:  全局过滤器实现鉴权
 */
@Slf4j
public class AuthFilter implements GlobalFilter, Ordered {

    @Value("${auth-service.authorizeUrl}")
    public String authorizeUrl;

    @Autowired
    RestTemplate restTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("=============Gateway AuthFilter=============");
        //前置过滤
        ServerHttpRequest request = exchange.getRequest();
        URI uri = request.getURI();
        String path = request.getPath().toString();
        MultiValueMap<String, String> queryParams = request.getQueryParams();
        String userId = queryParams.getFirst("userId");
        String businessCode = queryParams.getFirst("businessCode");
        for (Map.Entry<String, List<String>> stringListEntry : queryParams.entrySet()) {
            log.info("{} = {}",stringListEntry.getKey(),stringListEntry.getValue());
        }
        log.info("uri : "+uri);
        log.info("path : "+path);
        //TODO 2022年08月31日13:00:23
        HttpHeaders headers = new HttpHeaders();
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId",userId);
        map.put("businessCode",businessCode);
        map.put("targetApi",path);
        HttpEntity httpEntity = new HttpEntity<>(map, headers);
        ResponseEntity<Boolean> responseEntity = restTemplate.exchange(authorizeUrl, HttpMethod.POST, httpEntity, Boolean.class);
        Boolean result = responseEntity.getBody();
        log.info("result：{}",result);

        if (result){
            log.info("{}-{} 访问 {}",businessCode,userId,path);
            return chain.filter(exchange);
        }else {
            log.error("{}-{} 无权访问 {}",businessCode,userId,path);
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}

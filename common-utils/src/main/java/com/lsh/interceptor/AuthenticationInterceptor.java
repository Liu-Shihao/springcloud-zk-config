package com.lsh.interceptor;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.lsh.utils.BeanUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Date;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/24 14:41
 * @Desc:
 */
public class AuthenticationInterceptor implements HandlerInterceptor {

//    @Autowired
    HazelcastInstance hazelcastInstance = BeanUtils.getBean(HazelcastInstance.class);;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("-------- AuthenticationInterceptor.preHandle --- ");
        String username = request.getHeader("username");
        if ("".equals(username)|| username == null){
            System.out.println("username为空，不允许访问！");
            response.reset();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"不允许访问");
//            response.setCharacterEncoding("UTF-8");
//            response.setContentType("application/json;charset=UTF-8");
//            PrintWriter writer=response.getWriter();
//            writer.println("没有权限访问");
//            writer.flush();
//            writer.close();
            return false;
        }

        IMap<Object, Object> cache = hazelcastInstance.getMap("zk-cache");
        String data = (String) cache.get("test");
        System.out.println("Cache: "+data);
        System.out.println("Request URL: " + request.getRequestURL());
        System.out.println("Time"+new Date());
        if (data.contains(username)){
            System.out.println("允许访问");
            return true;
        }
        System.out.println("没有权限访问");
        response.reset();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter writer=response.getWriter();
            writer.println("没有权限访问");
            writer.flush();
            writer.close();
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        System.out.println("postHandle");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        System.out.println("afterCompletion");
    }
}

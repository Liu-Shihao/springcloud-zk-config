package com.lsh.auth.interceptor;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/24 14:41
 * @Desc: 鉴权操作
 */
public class AuthenticationInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("-------- AuthenticationInterceptor.preHandle --- ");
        String username = request.getHeader("username");
        if ("".equals(username)|| username == null){
            System.out.println("username为空，不允许访问！");
            response.reset();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            JSONObject res = new JSONObject();
            res.put( "code", "false");
            res.put( "msg", "username为空，不允许访问！");
            PrintWriter writer=response.getWriter();
            writer.append(res.toString());
//            writer.println(res.toString());
            writer.flush();
            writer.close();
            return false;
        }
        if (1==1){
            System.out.println("允许访问");
            return true;
        }
        System.out.println("没有权限访问");
        response.reset();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        JSONObject res = new JSONObject();
        res.put( "code", "false");
        res.put( "msg", "没有权限访问！");
        PrintWriter writer=response.getWriter();
        writer.println(res.toString());
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

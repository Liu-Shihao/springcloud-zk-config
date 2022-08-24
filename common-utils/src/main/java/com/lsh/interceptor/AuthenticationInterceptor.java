package com.lsh.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;

/**
 * @Author: LiuShihao
 * @Date: 2022/8/24 14:41
 * @Desc:
 */
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("-------- AuthenticationInterceptor.preHandle start--- ");
//        long startTime = System.currentTimeMillis();
        HttpSession session=request.getSession();
        System.out.println("Request URL: " + request.getRequestURL());
//        request.setAttribute("startTime", startTime);
        System.out.println("Time"+new Date());
        System.out.println("-------- AuthenticationInterceptor.preHandle end--- ");
        return true;
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

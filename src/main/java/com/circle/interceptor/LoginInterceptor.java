package com.circle.interceptor;

import com.circle.utils.UserHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author fangshaolei
 * @version 1.0.0
 * @ClassName LoginInterceptor
 * @Description 是一个登录的拦截器
 * @createTime 2022/05/13 9:07
 **/
public class LoginInterceptor implements HandlerInterceptor {
    /**
      * @author: fangshaolei
      * @description: 登录拦截逻辑
      * @Date: 2022/5/13 15:42
      * @params:
      * @return:
      **/
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//    判断是否需要拦截
        if(UserHolder.getUser() != null) return true;

        response.setStatus(404);
        return false;

    }
}

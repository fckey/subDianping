package com.circle.interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.circle.dto.UserDTO;
import com.circle.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.circle.utils.RedisConstants.LOGIN_USER_KEY;
import static com.circle.utils.RedisConstants.LOGIN_USER_TTL;

/**
 * @author fangshaolei
 * @version 1.0.0
 * @ClassName RefreshTokenInterceptor
 * @Description 对redis的登录状态进行刷新的工作
 * @createTime 2022/05/13 15:30
 **/
public class RefreshTokenInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }
    /**
      * @author: fangshaolei
      * @description:  对redis进行刷新
      * @Date: 2022/5/13 15:32
      * @params: 
      * @return: 
      **/
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//      TODO. 1. 获取请求头中的token
        String token = request.getHeader("authorization");
//        如果为空，直接放行，交给下一个来操作
        if(StrUtil.isBlank(token)) return true;

//        TODO 2. 基于Token获取redis中的用户
        String key = LOGIN_USER_KEY + token;
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);
//        判断redis中是否存在，不存在，交给下一个拦截器来处理
        if(userMap.isEmpty()) return true;

//        TODO 3. 将查询到的Map转成userDTO,
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
//        TODO 4. 将用户信息保存到ThreadLocal
        UserHolder.saveUser(userDTO);
//        TODO 5. 刷新token有效期
        stringRedisTemplate.expire(key, LOGIN_USER_TTL, TimeUnit.SECONDS);

//        放行
        return true;
    }

    /**
      * @author: fangshaolei
      * @description: 释放资源
      * @Date: 2022/5/13 15:40
      * @params: 
      * @return: 
      **/
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}

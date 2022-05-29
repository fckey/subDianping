package com.circle.config;

import com.circle.interceptor.LoginInterceptor;
import com.circle.interceptor.RefreshTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author fangshaolei
 * @version 1.0.0
 * @ClassName WebMvcConfig
 * @Description web相关的配置信息
 * @createTime 2022/05/13 9:11
 **/
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    /**
      * @author: fangshaolei
      * @description:  加入拦截器
      * @Date: 2022/5/13 15:04
      * @params:
      * @return:
      **/
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        登录的拦截器
        registry.addInterceptor(new LoginInterceptor())
                .excludePathPatterns(
                        "/shop/**",
                        "/voucher/**",
                        "/shop-type/**",
                        "/upload/**",
                        "/blog/hot",
                        "/user/code",
                        "/user/login"
                ).order(1);
//        刷新的拦截器
        registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate))
                .addPathPatterns("/**").order(0);
    }
}

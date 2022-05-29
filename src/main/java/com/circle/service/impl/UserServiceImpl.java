package com.circle.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.circle.dto.LoginFormDTO;
import com.circle.dto.Result;
import com.circle.dto.UserDTO;
import com.circle.entity.User;
import com.circle.mapper.UserMapper;
import com.circle.service.IUserService;
import com.circle.utils.RegexUtils;
import com.circle.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static com.circle.utils.RedisConstants.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    /**
      * @author: fangshaolei
      * @description: 发送验证码
      * @Date: 2022/5/13 14:42
      * @params: 
      * @return: 
      **/
    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1. 校验手机号
        if(RegexUtils.isPhoneInvalid(phone)) return Result.fail("手机号格式错误");
        // 2. 生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 3. 保存验证码到redis中，并设置有效期为2分钟
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        // 4. 发送短信验证码
        log.debug("发送短信验证码成功，验证码{}", code);
//       返回ok
        return Result.ok();
    }
    /**
      * @author: fangshaolei
      * @description: 登录逻辑
      * @Date: 2022/5/13 14:41
      * @params: 
      * @return: 
      **/
    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
//        1. 用户提交手机号和验证码
        String phone = loginForm.getPhone();
        if(RegexUtils.isPhoneInvalid(phone)) return Result.fail("手机号格式错误");
//        2. 校验验证码
//        TODO. 从Redis中获取验证码并进行校验
//        Object cacheCode = session.getAttribute(SystemConstants.SESSION_MESSAGE_CODE_FLAG); // 获取后端发送的验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);

        String loginCode = loginForm.getCode(); // 登录页面传过来的验证码
//        验证redis是否一致
        if(cacheCode == null || !cacheCode.equals(loginCode)) {
            // 不一致，报错
            return Result.fail("验证码错误");
        }
//      3. 一致，根据手机号查询用户
        User user = query().eq("phone", phone).one();
//       4. 判断用户是否存在，存在，直接登录，不存在，就直接创建用户并进行保存
        if(user == null){
            user = createUserWithPhone(phone);
        }

//      TODO. 5. 保存用户到Redis中

//          5.1 生成随机的token
        String token = UUID.randomUUID().toString(true);// 使用简单的字符即可
//         5.2 将user对象转换为userDTO
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
//         5.3 存储
//           notice: java.base/java.lang.Long cannot be cast to java.base/java.lang.String 所有的stringRedisTemplate的对象类型都是string,需要手动转换
        stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY + token, BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fileName, fileValue) -> fileValue.toString())));
//          5.4 设置有效期为一个小时，一个小时内没有操作，就直接失效
        stringRedisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.SECONDS);

//       6. 返回token
        return Result.ok(token);
    }

    private User createUserWithPhone(String phone) {
//        1. 创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomNumbers(10));
//        2. 保存用户信息
        save(user);
        return user;
    }
}

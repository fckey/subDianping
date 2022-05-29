package com.circle.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.circle.dto.Result;
import com.circle.entity.ShopType;
import com.circle.mapper.ShopTypeMapper;
import com.circle.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.circle.utils.RedisConstants.CACHE_TYPE_LIST_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    /**
      * @author: fangshaolei
      * @description: 加入redis缓存
      * @Date: 2022/5/13 16:29
      * @params: 
      * @return: 
      **/
    @Override
    public Result queryTypeList() {
//      1. 从redis中查询
        String key = CACHE_TYPE_LIST_KEY;
        String listJson = stringRedisTemplate.opsForValue().get(key);
//        2. 判断是否存在，如果有直接返回
        if(StrUtil.isNotBlank(listJson)) return Result.ok(JSONUtil.toList(listJson, ShopType.class));

//        3. 不存在，查询数据库
        List<ShopType> list = query().orderByAsc("sort").list();
        if(null == list) return Result.fail("店铺列表不存在");
//        存在，放入redis中一份
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(list));

        return Result.ok(list);
    }
}

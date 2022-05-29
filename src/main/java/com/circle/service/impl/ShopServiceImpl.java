package com.circle.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.circle.dto.Result;
import com.circle.entity.Shop;
import com.circle.mapper.ShopMapper;
import com.circle.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static com.circle.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    /**
      * @author: fangshaolei
      * @description:  加上redis的缓存优化，来对shop的查询过程来进行
      * @Date: 2022/5/13 16:05
      * @params: 
      * @return: 
      **/
    @Override
    public Result queryById(Long id) {
//                TODO. 1 从redis查询商铺缓存
        String key = CACHE_SHOP_KEY + id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);
//        2. 判断是否存在
        if(StrUtil.isNotBlank(shopJson)){
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return Result.ok(shop);
        }

        // TODO. 判断命中是否是空值，解决缓存穿透问题
        if(shopJson != null) return Result.fail("店铺信息不存在");

//        3. 不存在, 根据id查数据库
        Shop shop = getById(id);
//        4. 数据库不存在，直接返回
        if(null == shop) {
//           解决缓存穿透问题
//            将空值写入到redis中
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            return Result.fail("店铺不存在");
        }

//        5. 存在则设置redis，并设置过期时间
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);

//        6. 返回
        return Result.ok(shop);
    }
    /**
      * @author: fangshaolei
      * @description: 
      * @Date: 2022/5/13 17:04
      * @params: 
      * @return: 
      **/

    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if(id == null) return Result.fail("店铺id不能为空");

//        先更新数据库，之后在删除缓存
        updateById(shop);
//        2. 删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + id);

        return Result.ok();
    }
}

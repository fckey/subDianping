package com.circle.service.impl;

import cn.hutool.core.util.BooleanUtil;
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
      * 5@author: fangshaolei
      * @description:  加上redis的缓存优化，来对shop的查询过程来进行
      * @Date: 2022/5/13 16:05
      * @params: 
      * @return: 
      **/
    @Override
    public Result queryById(Long id) {
        // 缓存穿透的解决方案
//        Shop shop = queryWithPassThrough(id);
        // 缓存击穿问题的解决方案
        Shop shop = queryWithMutex(id);
        if(shop == null){
            return Result.fail("店铺信息不存在");
        }
        return Result.ok(shop);
    }
    /**
      * @author: fangshaolei
      * @description: 解决缓存的击穿问题
      * @Date: 2022/6/6 9:04
      * @params:
      * @return:
      **/
    public Shop queryWithPassThrough(Long id){
        //                TODO. 1 从redis查询商铺缓存
        String key = CACHE_SHOP_KEY + id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);
//        2. 判断是否存在
        if(StrUtil.isNotBlank(shopJson)){
            return JSONUtil.toBean(shopJson, Shop.class);
        }

        // TODO. 判断命中是否是空值，解决缓存穿透问题
        if(shopJson != null) return null;

//        3. 不存在, 根据id查数据库
        Shop shop = getById(id);
//        4. 数据库不存在，直接返回
        if(null == shop) {
//           解决缓存穿透问题
//            将空值写入到redis中
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }

//        5. 存在则设置redis，并设置过期时间
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);

//        6. 返回
        return shop;
    }
    /**
      * @author: fangshaolei
      * @description: 缓存击穿问题解决方案
      * @Date: 2022/6/6 9:07
      * @params:
      * @return:
      **/
    public Shop queryWithMutex(Long id){
//      TODO. 1 从redis查询商铺缓存
        String key = CACHE_SHOP_KEY + id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);
//        2. 判断是否存在
        if(StrUtil.isNotBlank(shopJson)){
            return JSONUtil.toBean(shopJson, Shop.class);
        }

        // TODO. 判断命中是否是空值，解决缓存穿透问题
        if(shopJson != null) return null;
        // 3. 实现缓存重建
        // 3.1  获取互斥锁
        String lockKey = "lock:shop:" + id;
        Shop shop = null;
        try {
            boolean isLock = tryLock(lockKey);
            // 判断锁是否获取成功
            if(!isLock){
                // 失败，则休眠重试
                Thread.sleep(50);
                // 递归调用
                return queryWithMutex(id);
            }

            shop = getById(id);
            // 模拟业务较复杂的key
            Thread.sleep(200);
//        4. 数据库不存在，直接返回
            if(null == shop) {
    //           解决缓存穿透问题
    //            将空值写入到redis中
                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }
//        5. 存在则设置redis，并设置过期时间
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 释放互斥锁
            unlock(lockKey);
        }
//        6. 返回
        return shop;
    }

    /**
      * @author: fangshaolei
      * @description: 
      * @Date: 2022/5/13 17:04
      * @params: 
      * @return: 
      **/

    @Override
    @Transactional // 单体项目、直接通过事务来进行控制
    public Result update(Shop shop) {
        Long id = shop.getId();
        // 判断id是否是存在的
        if(id == null) return Result.fail("店铺id不能为空");

//        先更新数据库，之后在删除缓存
        updateById(shop);
//        2. 删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + id);

        return Result.ok();
    }

    /**
     * @author: fangshaolei
     * @description: 互斥锁来解决热点key问题
     * @Date: 2022/6/6 8:50
     * @params:
     * @return:
     **/
    public boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        // 由于在拆箱的过程中，可能会出现指针的问题
        return BooleanUtil.isTrue(flag);
    }

    /**
      * @author: fangshaolei
      * @description: 释放锁的操作
      * @Date: 2022/6/6 9:01
      * @params:
      * @return:
      **/
    private void unlock(String key){
        stringRedisTemplate.delete(key);
    }

}

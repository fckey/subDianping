package com.circle.service;

import com.circle.dto.Result;
import com.circle.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IShopService extends IService<Shop> {
    /**
      * @author: fangshaolei
      * @description: 
      * @Date: 2022/5/13 16:05
      * @params: 
      * @return: 
      **/
    Result queryById(Long id);
    /**
      * @author: fangshaolei
      * @description: 更新商户信息
      * @Date: 2022/5/13 17:04
      * @params: 
      * @return: 
      **/
    Result update(Shop shop);
}

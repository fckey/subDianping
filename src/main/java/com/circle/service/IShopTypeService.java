package com.circle.service;

import com.circle.dto.Result;
import com.circle.entity.ShopType;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IShopTypeService extends IService<ShopType> {
    /**
      * @author: fangshaolei
      * @description: 查询店铺类型列表
      * @Date: 2022/5/13 16:18
      * @params: 
      * @return: 
      **/
    Result queryTypeList();
}

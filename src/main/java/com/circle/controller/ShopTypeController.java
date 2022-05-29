package com.circle.controller;


import com.circle.dto.Result;
import com.circle.service.IShopTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {
    @Resource
    private IShopTypeService typeService;
    
    /**
      * @author: fangshaolei
      * @description: 查询店铺类型的列表
      * @Date: 2022/5/13 16:17
      * @params: 
      * @return: 
      **/
    @GetMapping("list")
    public Result queryTypeList() {
        return typeService.queryTypeList();
    }
}

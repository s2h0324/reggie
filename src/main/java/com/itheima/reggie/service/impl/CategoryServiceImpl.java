package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;
    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //查询当前分类是否关联了菜品，如果关联，抛出一个业务异常
       dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
       int count = dishService.count(dishLambdaQueryWrapper);
       if(count > 0){
           //已经关联菜品，抛出异常
           throw new CustomException("当前分类下关联了菜品，不能删除");
       }
        //是否关联了套餐
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper =  new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count1 = setmealService.count(setmealLambdaQueryWrapper);
        if(count1 > 0){
            //抛出异常
            throw new CustomException("当前分类下关联了套餐，不能删粗");
        }
        //正常删除分类
        super.removeById(id);
    }
}

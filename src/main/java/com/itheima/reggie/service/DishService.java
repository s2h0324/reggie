package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
    //新增菜品，同时插入菜品对应的口味数据，需要操作两张表,dish\dish_falovr
    public void saveWithFlavor(DishDto dishDto);
    //根据Id查询菜品信息和对应的口味信息
    public DishDto getByIdWithFlavor(Long id);
    public void upDateWithFlavor(DishDto dishDto);
    public void upDateWithStatus(Integer status,List<Long> ids);
    //批量删除
    public void removeWithDish(List<Long> ids);
}

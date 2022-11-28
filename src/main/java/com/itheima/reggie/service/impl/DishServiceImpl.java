package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    /**
     * 新增菜品，同时保存对应的口味数据
     * @param dishDto
     */
    @Autowired
    private DishFlavorService dishFlavorService;
    private DishService dishService;
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品到菜品表dish
        this.save(dishDto);
        Long dishId = dishDto.getId();
        //菜品口味->藏品口味表
        List<DishFlavor> flavor = dishDto.getFlavors();
        flavor.stream().map((item)->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());//flavors.forEach(f -> f.setDishId(dishId);
        dishFlavorService.saveBatch(flavor);
    }

    /**
     *
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息 dish表
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //查询当前菜品对应的的口味信息 dish_flavor表查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavor = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavor);
        return dishDto;
    }

    @Override
    @Transactional
    public void upDateWithFlavor(DishDto dishDto) {
            //更新菜品表
        this.updateById(dishDto);
        //删除口味表--dish_flavor表的Delete操作
        LambdaQueryWrapper<DishFlavor> queryWrap = new LambdaQueryWrapper();
        queryWrap.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrap);
        //添加口味表--dish_flavor表的Insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors.stream().map((item)->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);

    }

    @Override
    public void upDateWithStatus(Integer status, List<Long> ids) {
        LambdaQueryWrapper<Dish> queryWrap = new LambdaQueryWrapper<>();
        queryWrap.in(ids!=null,Dish::getId,ids);
        List<Dish> list = this.list(queryWrap);
        for(Dish dish:list){
            if(dish!=null) {
                dish.setStatus(status);
                this.updateById(dish);
            }
        }
    }

    @Override
    public void removeWithDish(List<Long> ids) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ids!=null,Dish::getId,ids);
        queryWrapper.eq(Dish::getStatus,1);
        int count = count(queryWrapper);
        if(count > 0){
            throw new CustomException("商品售卖中，不能删除");
        }
        this.removeByIds(ids);
        LambdaQueryWrapper<DishFlavor>  flavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        flavorLambdaQueryWrapper.in(DishFlavor::getDishId,ids);
        //删除表中的对应关系
        dishFlavorService.remove(flavorLambdaQueryWrapper);
    }

}

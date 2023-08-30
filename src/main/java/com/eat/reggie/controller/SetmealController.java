package com.eat.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eat.reggie.common.CustomException;
import com.eat.reggie.common.R;
import com.eat.reggie.dto.DishDto;
import com.eat.reggie.dto.SetmealDto;
import com.eat.reggie.entity.Category;
import com.eat.reggie.entity.Dish;
import com.eat.reggie.entity.Setmeal;
import com.eat.reggie.entity.SetmealDish;
import com.eat.reggie.service.DishService;
import com.eat.reggie.service.SetmealDishService;
import com.eat.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private DishService dishService;

    @PostMapping
    @CacheEvict(value = "setmealCache", allEntries = true)  //表示清理setmealCache分类下的所有缓存数据
    public R<String> save(@RequestBody SetmealDto setmealDto){

        setmealService.saveWithDish(setmealDto);

        return R.success("添加套餐成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        //分页构造器
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.eq(name != null, Setmeal::getName, name);
        //执行查询
        setmealService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id){
        SetmealDto setmealDto = setmealService.getByIdWithDish(id);

        if(setmealDto != null){
            return R.success(setmealDto);
        }

        return R.error("未查询到改套餐的信息");
    }

    @PutMapping
    @CacheEvict(value = "setmealCache", allEntries = true)  //表示清理setmealCache分类下的所有缓存数据
    public R<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.updateWithDish(setmealDto);

        return R.success("更新套餐信息成功");
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache", allEntries = true)  //表示清理setmealCache分类下的所有缓存数据
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}", ids);

        setmealService.removeWithDish(ids);

        return R.success("套餐删除成功");
    }

    @PostMapping("/status/{status}")
    public R<String> changeStatus(@PathVariable int status, @RequestParam List<Long> ids){
        log.info("ids:{}", ids);
        if(status == 1){
            //启售，要保证套餐包含的菜品均为启售状态才可启售套餐

            LambdaQueryWrapper<SetmealDish> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.in(SetmealDish::getSetmealId, ids);
            List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper1);

            List<Long> dishIds = setmealDishList.stream().map((item) -> item.getDishId()).collect(Collectors.toList());

            LambdaQueryWrapper<Dish> queryWrapper2 = new LambdaQueryWrapper<>();
            queryWrapper2.in(Dish::getId, dishIds);
            queryWrapper2.eq(Dish::getStatus, 0);
            int count = dishService.count(queryWrapper2);
            if(count > 0){
                throw new CustomException("套餐内有菜品停售，无法启售");
            }

            LambdaQueryWrapper<Setmeal> queryWrapper3 = new LambdaQueryWrapper<>();
            queryWrapper3.in(Setmeal::getId, ids);
            List<Setmeal> list = setmealService.list(queryWrapper3);
            for (Setmeal s: list) {
                s.setStatus(1);
            }

            setmealService.updateBatchById(list);

            return R.success("套餐启售成功");

        }
        if(status == 0){
            //停售
            LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(Setmeal::getId, ids);
            List<Setmeal> list = setmealService.list(queryWrapper);
            for (Setmeal s: list) {
                s.setStatus(0);
            }

            setmealService.updateBatchById(list);

            return R.success("套餐停售成功");
        }
        return null;
    }

    @GetMapping("/list")
    @Cacheable(value = "setmealCache", key = "#setmeal.categoryId + '_' + #setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal){
        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null,Setmeal::getStatus, 1);
        //添加排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }

    @GetMapping("/dish/{id}")
    public R<List<DishDto>> dish(@PathVariable Long id){
        //得到套餐内菜品的id
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> list = setmealDishService.list(queryWrapper);

        List<DishDto> dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            Dish dish = dishService.getById(item.getDishId());
            if(dish != null){
                BeanUtils.copyProperties(dish, dishDto);
                dishDto.setCopies(item.getCopies());
                return dishDto;
            }
            return null;
        }).collect(Collectors.toList());
/*
        List<Long> dishIds = list.stream().map((item) -> item.getDishId()).collect(Collectors.toList());

        //根据菜品id的list获取菜品信息
        LambdaQueryWrapper<Dish> dishQueryWrapper = new LambdaQueryWrapper<>();
        dishQueryWrapper.in(Dish::getId, dishIds);
        List<Dish> dishList = dishService.list(dishQueryWrapper);
*/

        return R.success(dishDtoList);
    }
}

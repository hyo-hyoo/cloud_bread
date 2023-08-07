package com.eat.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eat.reggie.common.BaseContext;
import com.eat.reggie.common.CustomException;
import com.eat.reggie.common.R;
import com.eat.reggie.dto.OrdersDto;
import com.eat.reggie.entity.Orders;
import com.eat.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据:{}");
        orderService.submit(orders);
        return R.success("下单成功");
    }

    /**
     * 用户页订单信息
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page<OrdersDto>> userPage(int page, int pageSize){
        log.info("page={}, pageSize={}", page, pageSize);

        Page<OrdersDto> orders = orderService.pageWithDetail(page, pageSize);

        return R.success(orders);

    }

    /**
     * 订单管理
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page<Orders>> page(int page, int pageSize, Long number, String beginTime, String endTime){
        log.info("page={}, pageSize={}", page, pageSize);
        log.info("beginTime={}, endTime=={}", beginTime, endTime);

        Page<Orders> pageInfo = new Page<>(page, pageSize);

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件
        queryWrapper.like(number != null, Orders::getNumber, number);
        queryWrapper.ge(beginTime != null, Orders::getOrderTime, beginTime);
        queryWrapper.le(endTime != null, Orders::getOrderTime, endTime);

        queryWrapper.orderByAsc(Orders::getStatus);
        queryWrapper.orderByDesc(Orders::getCheckoutTime);
        Page<Orders> orders = orderService.page(pageInfo, queryWrapper);

        if (orders == null){
            throw new CustomException("未查找到符合条件的订单");
        }

        return R.success(orders);
    }

    @PutMapping
    public R<String> changeStatus(@RequestBody Orders order){
        Orders orderById = orderService.getById(order.getId());

        if (orderById == null){
            throw new CustomException("订单id错误，修改状态失败");
        }

        orderById.setStatus(order.getStatus());
        orderService.updateById(orderById);

        return R.success("修改订单状态成功");
    }

    @PostMapping("/again")
    public void again(Long id){

    }


}

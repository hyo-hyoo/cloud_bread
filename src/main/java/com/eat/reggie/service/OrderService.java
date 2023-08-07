package com.eat.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.eat.reggie.common.R;
import com.eat.reggie.dto.OrdersDto;
import com.eat.reggie.entity.Orders;

public interface OrderService extends IService<Orders> {

    public void submit(Orders orders);

    /**
     * 用户页面订单详情显示
     * @param page
     * @param pageSize
     */
    public Page<OrdersDto> pageWithDetail(int page, int pageSize);
}

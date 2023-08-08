package com.eat.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eat.reggie.entity.ShoppingCart;

public interface ShoppingCartService extends IService<ShoppingCart> {

    public ShoppingCart add(ShoppingCart shoppingCart);
}

package com.zyj.gulimall.cart.service;

import com.zyj.gulimall.cart.vo.Cart;
import com.zyj.gulimall.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @program: gulimall
 * @ClassName CartService
 * @author: YaJun
 * @Date: 2021 - 12 - 27 - 21:45
 * @Package: com.zyj.gulimall.cart.service
 * @Description:
 */
public interface CartService {

    /**
     * 将商品添加到购物车
     * @param skuId
     * @param num
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    /**
     * 获取购物车中某个购物项
     * @param skuId
     * @return
     */
    CartItem getCartItem(Long skuId);

    /**
     * 获取整个购物车
     * @return
     */
    Cart getCart() throws ExecutionException, InterruptedException;

    /**
     * 清空购物车数据
     */
    void clearCart(String cartKey);

    /**
     * 更新购物项选中状态
     * @param skuId
     * @param check
     */
    void checkItem(Long skuId, Integer check);

    /**
     * 更新购物项数量
     * @param skuId
     * @param num
     */
    void countItem(Long skuId, Integer num);

    /**
     * 删除购物项
     * @param skuId
     */
    void deleteItem(Long skuId);

    /**
     * 获取当前用户的购物车中的已选购物项
     * @return
     */
    List<CartItem> getUserCartItems();
}

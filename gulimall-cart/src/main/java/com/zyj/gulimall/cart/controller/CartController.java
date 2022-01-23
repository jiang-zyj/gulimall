package com.zyj.gulimall.cart.controller;

import com.zyj.gulimall.cart.service.CartService;
import com.zyj.gulimall.cart.vo.Cart;
import com.zyj.gulimall.cart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @program: gulimall
 * @ClassName CartController
 * @author: YaJun
 * @Date: 2021 - 12 - 27 - 21:48
 * @Package: com.zyj.gulimall.cart.controller
 * @Description:
 */
@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @ResponseBody
    @GetMapping("/currentUserCartItems")
    public List<CartItem> getCurrentUserCartItems() {
        return cartService.getUserCartItems();
    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {
        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num) {
        cartService.countItem(skuId, num);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId, @RequestParam("check") Integer check) {
        cartService.checkItem(skuId, check);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 浏览器有一个cookie: user-key; 标识用户身份,一个月后过期;
     * 如果第一次使用jd的购物车功能,都会给一个临时的用户身份;
     * 浏览器以后保存,每次访问都会带上这个cookie;
     *
     * 登录: session有
     * 没登陆: 按照cookie里面带来的user-key来做
     * 第一次: 如果没有临时用户,帮忙创建一个临时用户
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {

        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);
        return "cartList";
    }

    /**
     * 添加商品到购物车
     *
     * RedirectAttributes ra
     *      ra.addFlashAttribute(); 将数据放在session里面，可以在页面取出，但是只能取一次
     *      ra.addAttribute("skuId", skuId); 将数据放在url后面
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num,
                            RedirectAttributes ra) throws ExecutionException, InterruptedException {

        cartService.addToCart(skuId, num);

        //model.addAttribute("skuId", skuId);
        ra.addAttribute("skuId", skuId);
        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }

    /**
     * 跳转到成功页
     * 我们给页面放的购物车查询的数据不在添加完后放，添加完后让它重定向到页面，然后让它再查一遍，
     * 而不是一直添加。这样就间接解决了重复提交问题。但是并没有彻底解决，还是会有重复提交的问题，
     * 但是页面不会在用户刷新时进行重复添加，而是会重复查询购物车数据。
     * 即：一个请求(/addToCart)负责添加商品，一个请求(/addToCartSuccess.html)负责展示商品
     * @param skuId
     * @param model
     * @return
     */
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccess(@RequestParam("skuId") Long skuId, Model model) {
        // 重定向到成功页面，再次查询购物车数据即可
        CartItem item = cartService.getCartItem(skuId);
        model.addAttribute("item", item);
        return "success";
    }

}

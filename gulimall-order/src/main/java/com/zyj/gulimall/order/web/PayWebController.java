package com.zyj.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.zyj.gulimall.order.config.AlipayTemplate;
import com.zyj.gulimall.order.service.OrderService;
import com.zyj.gulimall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @program: gulimall
 * @ClassName PayWebController
 * @author: YaJun
 * @Date: 2022 - 02 - 09 - 20:51
 * @Package: com.zyj.gulimall.order.web
 * @Description: 支付宝支付
 */
@Controller
public class PayWebController {

    @Autowired
    private AlipayTemplate alipayTemplate;

    @Autowired
    private OrderService orderService;

    /**
     * 1、将支付页让浏览器展示
     * 2、支付成功后，我们要跳到用户的订单列表页
     * @param orderSn
     * @return
     * @throws AlipayApiException
     */
    @ResponseBody
    @GetMapping(value = "/payOrder", produces = "text/html")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
        // 生成PayVo数据
        PayVo payVo = orderService.getOrderPay(orderSn);
        // 返回的是一个页面，将此页面直接交给浏览器就行
        String pay = alipayTemplate.pay(payVo);
        System.out.println(pay);
        return pay;
    }

}

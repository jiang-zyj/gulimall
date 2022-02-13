package com.zyj.gulimall.member.web;

import com.alibaba.fastjson.JSON;
import com.zyj.common.utils.R;
import com.zyj.gulimall.member.feign.OrderFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: gulimall
 * @ClassName MemberWebController
 * @author: YaJun
 * @Date: 2022 - 02 - 09 - 21:38
 * @Package: com.zyj.gulimall.member.web
 * @Description:
 */
@Controller
public class MemberWebController {

    @Autowired
    private OrderFeignService orderFeignService;

    @GetMapping("/memberOrder.html")
    public String memberOrderPage(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                  Model model, HttpServletRequest request) {
        // 获取到支付宝给我们传来的所有请求数据
        // 从request获取支付宝签名，并验证签名，如果签名正确，则用户可以修改
        // 查出当前登录的用户的所有订单列表数据
        Map<String, Object> page = new HashMap<>();
        page.put("page", pageNum);
        R r = orderFeignService.listWithItem(page);
        System.out.println(JSON.toJSON(r));
        model.addAttribute("orders", r);
        return "orderList";
    }

}

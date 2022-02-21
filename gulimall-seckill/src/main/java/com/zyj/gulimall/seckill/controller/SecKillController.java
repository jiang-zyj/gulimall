package com.zyj.gulimall.seckill.controller;

import com.zyj.common.utils.R;
import com.zyj.gulimall.seckill.service.SecKillService;
import com.zyj.gulimall.seckill.to.SecKillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @program: gulimall
 * @ClassName SecKillController
 * @author: YaJun
 * @Date: 2022 - 02 - 20 - 18:15
 * @Package: com.zyj.gulimall.seckill.controller
 * @Description:
 */
@Controller
public class SecKillController {

    @Autowired
    private SecKillService secKillService;

    /**
     * 返回当前时间可以参与的秒杀商品信息
     * @return
     */
    @ResponseBody
    @GetMapping("/currentSecKillSkus")
    public R getCurrentSecKillSkus() {
        List<SecKillSkuRedisTo> vos = secKillService.getCurrentSecKillSkus();
        return R.ok().setData(vos);
    }

    @ResponseBody
    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSecKillInfo(@PathVariable("skuId") Long skuId) {
        SecKillSkuRedisTo to = secKillService.getSkuSecKillInfo(skuId);
        return R.ok().setData(to);
    }

    @GetMapping("kill")
    public String secKill(@RequestParam("killId") String killId,
                          @RequestParam("key") String key,
                          @RequestParam("num") Integer num,
                          Model model) {

        String orderSn = secKillService.kill(killId, key, num);
        model.addAttribute("orderSn", orderSn);
        // 1. 判断是否登录
        return "success";
    }

}

package com.zyj.gulimall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.zyj.gulimall.order.config.AlipayTemplate;
import com.zyj.gulimall.order.service.OrderService;
import com.zyj.gulimall.order.vo.PayAsyncVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @program: gulimall
 * @ClassName OrderPayedListener
 * @author: YaJun
 * @Date: 2022 - 02 - 10 - 21:16
 * @Package: com.zyj.gulimall.order.listener
 * @Description: 用于接收支付宝异步的通知
 */
@RestController
public class OrderPayedListener {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AlipayTemplate alipayTemplate;

    @PostMapping("/payed/notify")
    public String handleAliPayed(PayAsyncVo vo, HttpServletRequest request) throws AlipayApiException, UnsupportedEncodingException {
        // 只要我们接收到了支付宝给我们异步的通知，告诉我们订单支付成功，就回复success，支付宝就不再继续通知
        //Map<String, String[]> parameterMap = request.getParameterMap();
        //for (String key : parameterMap.keySet()) {
        //    String value = request.getParameter(key);
        //    System.out.println("参数名：" + key + "==>参数值：" + value);
        //}

        // 验签
        //获取支付宝POST过来反馈信息
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = iter.next();
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayTemplate.getAlipay_public_key(), alipayTemplate.getCharset(), alipayTemplate.getSign_type()); //调用SDK验证签名
        if (signVerified) {
            System.out.println("签名验证成功...");
            String result = orderService.handlePayResult(vo);
            return result;
        } else {
            System.out.println("签名验证失败...");
            return "error";
        }
    }

}

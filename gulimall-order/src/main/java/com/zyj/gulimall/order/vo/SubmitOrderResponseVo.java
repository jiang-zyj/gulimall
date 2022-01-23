package com.zyj.gulimall.order.vo;

import com.zyj.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @program: gulimall
 * @ClassName SubmitOrderResponseVo
 * @author: YaJun
 * @Date: 2022 - 01 - 13 - 21:42
 * @Package: com.zyj.gulimall.order.vo
 * @Description: 下单操作的返回数据
 */
@Data
public class SubmitOrderResponseVo {

    /**
     * 订单实体数据
     */
    private OrderEntity order;

    /**
     * 错误状态码：0 表示成功
     */
    private Integer code;

}

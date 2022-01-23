package com.zyj.gulimall.ware.vo;

import lombok.Data;

/**
 * @program: gulimall
 * @ClassName LockStockResult
 * @author: YaJun
 * @Date: 2022 - 01 - 15 - 23:33
 * @Package: com.zyj.gulimall.ware.vo
 * @Description: 库存锁定结果
 */
@Data
public class LockStockResult {

    /**
     * 商品的skuId
     */
    private Long skuId;

    /**
     * 锁定几件
     */
    private Integer num;

    /**
     * 是否锁定成功
     */
    private Boolean locked;

}

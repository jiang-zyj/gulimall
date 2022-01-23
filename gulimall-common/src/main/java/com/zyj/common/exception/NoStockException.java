package com.zyj.common.exception;

/**
 * @program: gulimall
 * @ClassName NoStockException
 * @author: YaJun
 * @Date: 2022 - 01 - 15 - 23:55
 * @Package: com.zyj.gulimall.ware.exception
 * @Description: 没有库存的异常
 */
public class NoStockException extends RuntimeException {

    private Long skuId;

    public NoStockException(Long skuId) {
        super("商品id：" + skuId + "：库存不足");
    }

    public NoStockException(String msg) {
        super(msg);
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}

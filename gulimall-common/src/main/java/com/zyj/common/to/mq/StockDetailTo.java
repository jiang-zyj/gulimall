package com.zyj.common.to.mq;

import lombok.Data;

/**
 * @program: gulimall
 * @ClassName StockDetailTo
 * @author: YaJun
 * @Date: 2022 - 01 - 20 - 21:07
 * @Package: com.zyj.common.to.mq
 * @Description: 库存详情To
 */
@Data
public class StockDetailTo {

    /**
     * id
     */
    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * sku_name
     */
    private String skuName;
    /**
     * 购买个数
     */
    private Integer skuNum;
    /**
     * 工作单id
     */
    private Long taskId;
    /**
     * 仓库id
     */
    private Long wareId;
    /**
     * 1-已锁定  2-已解锁  3-扣减
     */
    private Integer lockStatus;

}

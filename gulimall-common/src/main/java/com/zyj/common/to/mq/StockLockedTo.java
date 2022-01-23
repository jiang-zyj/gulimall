package com.zyj.common.to.mq;

import lombok.Data;

/**
 * @program: gulimall
 * @ClassName StockLockedTo
 * @author: YaJun
 * @Date: 2022 - 01 - 20 - 20:58
 * @Package: com.zyj.common.to.mq
 * @Description: 库存锁定To
 */
@Data
public class StockLockedTo {

    /**
     * 库存工作单的id
     */
    private Long id;

    /**
     * 工作单详情
     */
    private StockDetailTo detail;

}

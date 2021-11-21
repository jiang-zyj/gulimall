package com.zyj.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @program: gulimall
 * @ClassName SkuReductionTo
 * @author: YaJun
 * @Date: 2021 - 11 - 20 - 17:05
 * @Package: com.zyj.common.to
 * @Description:
 */
@Data
public class SkuReductionTo {

    private Long skuId;

    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    private List<MemberPrice> memberPrice;

}

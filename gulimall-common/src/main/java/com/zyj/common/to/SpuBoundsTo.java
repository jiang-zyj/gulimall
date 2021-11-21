package com.zyj.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @program: gulimall
 * @ClassName SpuBoundsTo
 * @author: YaJun
 * @Date: 2021 - 11 - 20 - 16:53
 * @Package: com.zyj.common.to
 * @Description: SpuBoundsTo
 */
@Data
public class SpuBoundsTo {

    private Long spuId;

    private BigDecimal buyBounds;
    private BigDecimal growBounds;

}

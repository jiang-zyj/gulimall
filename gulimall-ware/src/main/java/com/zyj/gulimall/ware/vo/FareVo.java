package com.zyj.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @program: gulimall
 * @ClassName FareVo
 * @author: YaJun
 * @Date: 2022 - 01 - 12 - 21:43
 * @Package: com.zyj.gulimall.ware.vo
 * @Description: 运费返回数据：地址信息 + 运费信息
 */
@Data
public class FareVo {

    /**
     * 地址信息
     */
    private MemberAddressVo address;

    /**
     * 运费信息
     */
    private BigDecimal fare;

}

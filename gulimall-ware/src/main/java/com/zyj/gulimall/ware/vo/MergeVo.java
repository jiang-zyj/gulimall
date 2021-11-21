package com.zyj.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @program: gulimall
 * @ClassName MergeVo
 * @author: YaJun
 * @Date: 2021 - 11 - 21 - 15:10
 * @Package: com.zyj.gulimall.ware.vo
 * @Description:
 */
@Data
public class MergeVo {

    // items: [1, 2]
    //purchaseId: 1

    private Long purchaseId;

    private List<Long> items;
}

package com.zyj.gulimall.ware.vo;

import lombok.Data;

/**
 * @program: gulimall
 * @ClassName PurchaseItemDoneVo
 * @author: YaJun
 * @Date: 2021 - 11 - 21 - 18:26
 * @Package: com.zyj.gulimall.ware.vo
 * @Description:
 */
@Data
public class PurchaseItemDoneVo {

    /**
     * {"itemId": 4, "status": 3, "reason":""},
     */

    /**
     * 采购项id
     */
    private Long itemId;

    /**
     * 采购状态
     */
    private Integer status;

    /**
     * 失败原因
     */
    private String reason;

}

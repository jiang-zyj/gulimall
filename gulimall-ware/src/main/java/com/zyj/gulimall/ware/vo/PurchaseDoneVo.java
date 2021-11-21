package com.zyj.gulimall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @program: gulimall
 * @ClassName PurchaseDoneVo
 * @author: YaJun
 * @Date: 2021 - 11 - 21 - 18:24
 * @Package: com.zyj.gulimall.ware.vo
 * @Description:
 */
@Data
public class PurchaseDoneVo {

    /**
     * {"id": 3, "items": [
     *     {"itemId": 4, "status": 3, "reason":""},
     *     {"itemId": 4, "status": 4, "reason":"无货"}
     * ]}
     */
    /**
     * 采购单id
     */
    @NotNull
    private Long id;

    /**
     * 采购项
     */
    private List<PurchaseItemDoneVo> items;
}

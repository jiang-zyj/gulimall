package com.zyj.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zyj.common.utils.PageUtils;
import com.zyj.gulimall.ware.entity.PurchaseEntity;
import com.zyj.gulimall.ware.vo.MergeVo;
import com.zyj.gulimall.ware.vo.PurchaseDoneVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author Jiang
 * @email 2412305937@qq.com
 * @date 2021-11-01 22:38:04
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnReceivePurchase(Map<String, Object> params);

    void mergePurchaseDetails(MergeVo mergeVo);

    void receivedPurchase(List<Long> ids);

    void done(PurchaseDoneVo doneVo);
}


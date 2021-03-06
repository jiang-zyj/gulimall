package com.zyj.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zyj.common.to.mq.OrderTo;
import com.zyj.common.to.mq.StockLockedTo;
import com.zyj.common.utils.PageUtils;
import com.zyj.gulimall.ware.entity.WareSkuEntity;
import com.zyj.gulimall.ware.vo.SkuHasStockVo;
import com.zyj.gulimall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author Jiang
 * @email 2412305937@qq.com
 * @date 2021-11-01 22:38:04
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds);

    /**
     * 为某个订单锁定库存
     * @param vo
     * @return
     */
    Boolean orderLockStock(WareSkuLockVo vo);

    void unLockStock(StockLockedTo to);

    void unLockStock(OrderTo orderTo);
}


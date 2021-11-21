package com.zyj.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyj.common.constant.WareConstant;
import com.zyj.common.utils.PageUtils;
import com.zyj.common.utils.Query;
import com.zyj.gulimall.ware.dao.PurchaseDao;
import com.zyj.gulimall.ware.entity.PurchaseDetailEntity;
import com.zyj.gulimall.ware.entity.PurchaseEntity;
import com.zyj.gulimall.ware.service.PurchaseDetailService;
import com.zyj.gulimall.ware.service.PurchaseService;
import com.zyj.gulimall.ware.service.WareSkuService;
import com.zyj.gulimall.ware.vo.MergeVo;
import com.zyj.gulimall.ware.vo.PurchaseDoneVo;
import com.zyj.gulimall.ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Autowired
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnReceivePurchase(Map<String, Object> params) {

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", 0).or().eq("status", 1)
        );

        return new PageUtils(page);

    }

    /**
     * 合并采购需求
     *
     * @param mergeVo
     */
    @Transactional
    @Override
    public void mergePurchaseDetails(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        // 判断合并的时候是否选中了采购单
        if (purchaseId == null) {
            // 没有选中采购单
            // 1. 新建采购单
            PurchaseEntity purchaseEntity = new PurchaseEntity();

            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());

            this.save(purchaseEntity);

            purchaseId = purchaseEntity.getId();
        }

        // TODO: 确认采购单是0,1才可以合并
        // 合并采购需求（批量修改采购需求的属性）
        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(item -> {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            detailEntity.setId(item);
            detailEntity.setPurchaseId(finalPurchaseId);
            detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());

            return detailEntity;
        }).collect(Collectors.toList());

        purchaseDetailService.updateBatchById(collect);

        // 每次更新完后，还需要更新一下时间
        PurchaseEntity purchaseEntity = new PurchaseEntity();

        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);

    }

    /**
     * @param ids 采购单id
     */
    @Override
    public void receivedPurchase(List<Long> ids) {
        // 1. 确认当前采购单是新建或者已分配状态
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            PurchaseEntity byId = this.getById(id);
            return byId;
        }).filter(item -> {
            return item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                    item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode();
        }).map(item -> {
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVED.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());

        // 2. 改变采购单的状态
        this.updateBatchById(collect);

        // 3. 改变采购项的状态
        collect.forEach((item) -> {
            List<PurchaseDetailEntity> detailEntities = purchaseDetailService.listDetailByPurchaseId(item.getId());
            List<PurchaseDetailEntity> detailEntityList = detailEntities.stream().map(detailEntity -> {
                PurchaseDetailEntity entity = new PurchaseDetailEntity();
                entity.setId(detailEntity.getId());
                entity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return entity;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(detailEntityList);
        });
    }

    @Transactional
    @Override
    public void done(PurchaseDoneVo doneVo) {
        // 如果每个采购项都是成功的, 则采购单的状态为成功, 否则, 如果有一个采购项的状态为失败, 这采购单的状态就为失败


        // 2. 改变每一个采购项的状态
        boolean flag = true;
        List<PurchaseItemDoneVo> items = doneVo.getItems();
        // 根据请求中的数据,存储每一个采购项的状态
        List<PurchaseDetailEntity> updates = new ArrayList<>();
        for (PurchaseItemDoneVo item : items) {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()) {
                // 采购失败
                flag = false;
                detailEntity.setStatus(item.getStatus());
            } else {
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISHED.getCode());
                // 3. 将成功采购的进行入库
                // 先查询采购项信息
                PurchaseDetailEntity entity = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum());
            }
            detailEntity.setId(item.getItemId());
            updates.add(detailEntity);
        }

        // 批量更新
        purchaseDetailService.updateBatchById(updates);

        // 1. 改变采购单状态
        Long id = doneVo.getId();
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag?WareConstant.PurchaseStatusEnum.FINISHED.getCode():WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);


    }

}
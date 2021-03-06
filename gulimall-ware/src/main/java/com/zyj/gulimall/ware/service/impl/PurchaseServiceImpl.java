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
     * ??????????????????
     *
     * @param mergeVo
     */
    @Transactional
    @Override
    public void mergePurchaseDetails(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        // ?????????????????????????????????????????????
        if (purchaseId == null) {
            // ?????????????????????
            // 1. ???????????????
            PurchaseEntity purchaseEntity = new PurchaseEntity();

            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());

            this.save(purchaseEntity);

            purchaseId = purchaseEntity.getId();
        }

        // TODO: ??????????????????0,1???????????????
        // ?????????????????????????????????????????????????????????
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

        // ????????????????????????????????????????????????
        PurchaseEntity purchaseEntity = new PurchaseEntity();

        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);

    }

    /**
     * @param ids ?????????id
     */
    @Override
    public void receivedPurchase(List<Long> ids) {
        // 1. ???????????????????????????????????????????????????
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

        // 2. ????????????????????????
        this.updateBatchById(collect);

        // 3. ????????????????????????
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
        // ????????????????????????????????????, ??????????????????????????????, ??????, ??????????????????????????????????????????, ?????????????????????????????????


        // 2. ?????????????????????????????????
        boolean flag = true;
        List<PurchaseItemDoneVo> items = doneVo.getItems();
        // ????????????????????????,?????????????????????????????????
        List<PurchaseDetailEntity> updates = new ArrayList<>();
        for (PurchaseItemDoneVo item : items) {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()) {
                // ????????????
                flag = false;
                detailEntity.setStatus(item.getStatus());
            } else {
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISHED.getCode());
                // 3. ??????????????????????????????
                // ????????????????????????
                PurchaseDetailEntity entity = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum());
            }
            detailEntity.setId(item.getItemId());
            updates.add(detailEntity);
        }

        // ????????????
        purchaseDetailService.updateBatchById(updates);

        // 1. ?????????????????????
        Long id = doneVo.getId();
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag?WareConstant.PurchaseStatusEnum.FINISHED.getCode():WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);


    }

}
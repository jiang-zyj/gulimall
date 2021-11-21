package com.zyj.gulimall.ware.controller;

import com.zyj.common.utils.PageUtils;
import com.zyj.common.utils.R;
import com.zyj.gulimall.ware.entity.PurchaseEntity;
import com.zyj.gulimall.ware.service.PurchaseService;
import com.zyj.gulimall.ware.vo.MergeVo;
import com.zyj.gulimall.ware.vo.PurchaseDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;



/**
 * 采购信息
 *
 * @author Jiang
 * @email 2412305937@qq.com
 * @date 2021-11-01 22:38:04
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;


    /**
     * /ware/purchase/done
     * 完成采购单
     * @param doneVo
     * @return
     */
    @PostMapping("/done")
    public R done(@RequestBody PurchaseDoneVo doneVo){
        //
        purchaseService.done(doneVo);

        return R.ok();
    }

    /**
     * /ware/purchase/received      采购人员系统/ware/purchase/received
     * 领取采购单
     * @param ids
     * @return
     */
    @PostMapping("/received")
    public R received(@RequestBody List<Long> ids){
        //
        purchaseService.receivedPurchase(ids);

        return R.ok();
    }

    // /ware/purchase/merge
    @PostMapping("/merge")
    public R merge(@RequestBody MergeVo mergeVo){
        // 合并采购需求
        purchaseService.mergePurchaseDetails(mergeVo);

        return R.ok();
    }

    // ware/purchase/unreceive/list
    @RequestMapping("/unreceive/list")
    public R unReceiveList(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPageUnReceivePurchase(params);

        return R.ok().put("page", page);
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
        public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
        public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
        public R save(@RequestBody PurchaseEntity purchase){
        purchase.setCreateTime(new Date());
        purchase.setUpdateTime(new Date());
		purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
        public R update(@RequestBody PurchaseEntity purchase){
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
        public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}

package com.zyj.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zyj.common.utils.PageUtils;
import com.zyj.gulimall.product.entity.SpuInfoDescEntity;

import java.util.Map;

/**
 * spu信息介绍
 *
 * @author Jiang
 * @email 2412305937@qq.com
 * @date 2021-11-01 20:01:10
 */
public interface SpuInfoDescService extends IService<SpuInfoDescEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfoDesc(SpuInfoDescEntity descEntity);
}


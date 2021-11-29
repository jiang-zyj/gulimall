package com.zyj.gulimall.search.service;

import com.zyj.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @program: gulimall
 * @ClassName ProductSaveService
 * @author: YaJun
 * @Date: 2021 - 11 - 29 - 23:15
 * @Package: com.zyj.gulimall.search.service
 * @Description:
 */
public interface ProductSaveService {
    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}

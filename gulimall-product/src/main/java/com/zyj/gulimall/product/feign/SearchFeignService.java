package com.zyj.gulimall.product.feign;

import com.zyj.common.to.es.SkuEsModel;
import com.zyj.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @program: gulimall
 * @ClassName SearchFeignService
 * @author: YaJun
 * @Date: 2021 - 11 - 29 - 23:33
 * @Package: com.zyj.gulimall.product.feign
 * @Description:
 */
@FeignClient("gulimall-search")
public interface SearchFeignService {

    @PostMapping("/search/save/product")
    R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);

}

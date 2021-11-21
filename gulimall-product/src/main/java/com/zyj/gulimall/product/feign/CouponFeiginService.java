package com.zyj.gulimall.product.feign;

import com.zyj.common.to.SkuReductionTo;
import com.zyj.common.to.SpuBoundsTo;
import com.zyj.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @program: gulimall
 * @ClassName CouponFeiginService
 * @author: YaJun
 * @Date: 2021 - 11 - 20 - 16:50
 * @Package: com.zyj.gulimall.product.feign
 * @Description:
 */
@FeignClient("gulimall-coupon")
public interface CouponFeiginService {

    /**
     * SpringCloud 远程调用逻辑
     *  1. CouponFeignService.saveSpuBounds(SpuBoundsTo)
     *      1.1 @RequestBody将这个对象转为json
     *      1.2 找到gulimall-coupon服务，发送/coupon/spubounds/save请求，
     *          将上一步转的json放到请求体位置，发送请求
     *      1.3 对方服务收到请求，请求体里有json数据，
     *          (@RequestBody SpuBoundsEntity spuBounds)，将请求体里的json转为SpuBoundsEntity
     * 只要json数据模型是兼容的，双方服务无需使用同一个to
     * @param spuBoundsTo
     * @return
     */
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundsTo spuBoundsTo);

    @PostMapping("/coupon/skufullreduction/saveInfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}

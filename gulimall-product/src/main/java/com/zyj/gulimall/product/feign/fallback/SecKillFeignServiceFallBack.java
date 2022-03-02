package com.zyj.gulimall.product.feign.fallback;

import com.zyj.common.exception.BizCodeEnum;
import com.zyj.common.utils.R;
import com.zyj.gulimall.product.feign.SecKillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @program: gulimall
 * @ClassName SecKillFeignServiceFallBack
 * @author: YaJun
 * @Date: 2022 - 02 - 28 - 20:22
 * @Package: com.zyj.gulimall.product.feign.fallback
 * @Description:
 */
@Slf4j
@Component
public class SecKillFeignServiceFallBack implements SecKillFeignService {
    @Override
    public R getSkuSecKillInfo(Long skuId) {
        log.info("熔断方法调用...getSkuSecKillInfo");
        return R.error(BizCodeEnum.TOO_MANY_REQUEST.getCode(), BizCodeEnum.TOO_MANY_REQUEST.getMsg());
    }
}

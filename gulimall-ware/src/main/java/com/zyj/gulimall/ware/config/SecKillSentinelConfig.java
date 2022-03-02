package com.zyj.gulimall.ware.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.zyj.common.exception.BizCodeEnum;
import com.zyj.common.utils.R;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @program: gulimall
 * @ClassName SecKillSentinelConfig
 * @author: YaJun
 * @Date: 2022 - 02 - 21 - 22:10
 * @Package: com.zyj.gulimall.seckill.config
 * @Description: Sentinel自定义阻断异常处理类
 */
@Configuration
public class SecKillSentinelConfig implements BlockExceptionHandler {

    //private UrlBlockHandler urlBlockHandler;

    // 旧版
    //public SecKillSentinelConfig() {
    //    WebCallbackManager.setUrlBlockHandler(new UrlBlockHandler() {
    //        @Override
    //        public void blocked(HttpServletRequest request, HttpServletResponse response, BlockException e) throws IOException {
    //            R error = R.error(BizCodeEnum.TOO_MANY_REQUEST.getCode(), BizCodeEnum.TOO_MANY_REQUEST.getMsg());
    //            response.getWriter().write(JSON.toJSONString(error));
    //        }
    //    });
    //}

    // 新版；另外关于新版的sentinel的其他问题可以访问：https://www.cnblogs.com/exce-ben/p/13501972.html; https://www.cnblogs.com/hhhshct/p/14315821.html
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, BlockException e) throws Exception {
        R error = R.error(BizCodeEnum.TOO_MANY_REQUEST.getCode(), BizCodeEnum.TOO_MANY_REQUEST.getMsg());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write(JSON.toJSONString(error));
    }
}

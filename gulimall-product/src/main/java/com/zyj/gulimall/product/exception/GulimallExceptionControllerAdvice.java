package com.zyj.gulimall.product.exception;

import com.zyj.common.exception.BizCodeEnum;
import com.zyj.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: gulimall
 * @ClassName GulimallExceptionControllerAdvice
 * @author: YaJun
 * @Date: 2021 - 11 - 10 - 22:48
 * @Package: com.zyj.gulimall.product
 * @Description: 集中处理所有异常
 */
@Slf4j
//@ResponseBody
//@ControllerAdvice
@RestControllerAdvice(basePackages = "com.zyj.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e) {
        log.error("数据校验出现问题{}, 异常类型：{}",e.getMessage(),e.getClass());
        BindingResult bindingResult = e.getBindingResult();

        Map<String, String> errorMap = new HashMap<>();
        bindingResult.getFieldErrors().forEach((fieldError) -> {
            errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(), BizCodeEnum.VALID_EXCEPTION.getMsg()).put("data", errorMap);
    }

    //@ExceptionHandler(value = Exception.class)
    //public R handleException(Throwable throwable) {
    //    log.error("\n未知异常信息：{}, \n异常类型：{}", throwable.getMessage(), throwable.getMessage());
    //    return R.error(BizCodeEnum.UN_KNOW_EXCEPTION.getCode(), BizCodeEnum.UN_KNOW_EXCEPTION.getMsg());
    //}

}

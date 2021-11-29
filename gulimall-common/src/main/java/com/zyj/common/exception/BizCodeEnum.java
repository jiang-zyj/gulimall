package com.zyj.common.exception;

/**
 * @program: gulimall
 * @ClassName BizCodeEnum
 * @author: YaJun
 * @Date: 2021 - 11 - 11 - 20:47
 * @Package: com.zyj.common.exception
 * @Description: 异常信息
 */
public enum BizCodeEnum {

    UN_KNOW_EXCEPTION(10000, "系统未知异常"),
    VALID_EXCEPTION(10001, "参数格式校验失败"),
    PRODUCT_UP_EXCEPTION(11000, "商品上架异常");

    private final int code;
    private final String msg;
    BizCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}

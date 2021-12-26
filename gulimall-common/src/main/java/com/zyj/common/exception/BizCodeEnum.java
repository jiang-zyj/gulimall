package com.zyj.common.exception;

/**
 * @program: gulimall
 * @ClassName BizCodeEnum
 * @author: YaJun
 * @Date: 2021 - 11 - 11 - 20:47
 * @Package: com.zyj.common.exception
 * @Description: 异常信息
 * 错误码和错误信息定义类
 * 1. 错误码定义规则为5位数字
 * 2. 前两位表示业务场景，最后三位表示错误码。例如：10001。10：通用；001：系统未知异常
 * 3. 维护错误码后需要维护错误描述，将它们定义为枚举形式
 * 错误码列表：
 * 10：通用
 * 001：参数格式校验
 * 002：短信验证码频率太高
 * 11：商品
 * 12：订单
 * 13：购物车
 * 14：物流
 * 15：用户
 */
public enum BizCodeEnum {

    UN_KNOW_EXCEPTION(10000, "系统未知异常"),
    VALID_EXCEPTION(10001, "参数格式校验失败"),
    SMS_CODE_EXCEPTION(10002, "验证码获取频率太高，请稍后再试"),
    PRODUCT_UP_EXCEPTION(11000, "商品上架异常"),
    USER_EXIST_EXCEPTION(15001, "用户存在"),
    PHONE_EXIST_EXCEPTION(15002, "手机号存在"),
    LOGINACCT_PASSWORD_INVALID_EXCEPTION(15003, "账号密码错误");

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

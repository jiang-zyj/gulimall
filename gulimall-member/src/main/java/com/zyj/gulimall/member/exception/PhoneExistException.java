package com.zyj.gulimall.member.exception;

/**
 * @program: gulimall
 * @ClassName PhoneExistException
 * @author: YaJun
 * @Date: 2021 - 12 - 22 - 20:14
 * @Package: com.zyj.gulimall.member.exception
 * @Description:
 */
public class PhoneExistException extends RuntimeException{

    public PhoneExistException() {
        super("手机号已存在");
    }
}

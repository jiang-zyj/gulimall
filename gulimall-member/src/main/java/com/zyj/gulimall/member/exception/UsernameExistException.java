package com.zyj.gulimall.member.exception;

/**
 * @program: gulimall
 * @ClassName UsernameExistException
 * @author: YaJun
 * @Date: 2021 - 12 - 22 - 20:14
 * @Package: com.zyj.gulimall.member.exception
 * @Description:
 */
public class UsernameExistException extends RuntimeException{

    public UsernameExistException() {
        super("用户名已存在");
    }
}

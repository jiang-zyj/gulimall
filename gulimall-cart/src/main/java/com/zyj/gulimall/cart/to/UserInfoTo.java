package com.zyj.gulimall.cart.to;

import lombok.Data;
import lombok.ToString;

/**
 * @program: gulimall
 * @ClassName UserInfoTo
 * @author: YaJun
 * @Date: 2021 - 12 - 27 - 22:01
 * @Package: com.zyj.gulimall.cart.vo
 * @Description: 用户登录信息
 */
@ToString
@Data
public class UserInfoTo {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * user-key
     */
    private String userKey;

    /**
     * 是否有临时用户
     */
    private boolean tempUser = false;

}

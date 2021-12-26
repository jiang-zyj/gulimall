package com.zyj.gulimall.member.vo;

import lombok.Data;

/**
 * @program: gulimall
 * @ClassName SocialUser
 * @author: YaJun
 * @Date: 2021 - 12 - 25 - 16:30
 * @Package: com.zyj.gulimall.auth.vo
 * @Description: 社交用户
 */
@Data
public class SocialUser {

    private String access_token;
    private String remind_in;
    private long expires_in;
    private String uid;
    private String isRealName;

}

package com.zyj.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zyj.common.utils.PageUtils;
import com.zyj.gulimall.member.entity.MemberEntity;
import com.zyj.gulimall.member.exception.PhoneExistException;
import com.zyj.gulimall.member.exception.UsernameExistException;
import com.zyj.gulimall.member.vo.MemberLoginVo;
import com.zyj.gulimall.member.vo.MemberRegisterVo;
import com.zyj.gulimall.member.vo.SocialUser;

import java.util.Map;

/**
 * 会员
 *
 * @author Jiang
 * @email 2412305937@qq.com
 * @date 2021-11-01 22:22:48
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberRegisterVo registerVo);

    void checkPhoneUnique(String phone) throws PhoneExistException;

    void checkUsernameUnique(String username) throws UsernameExistException;

    MemberEntity login(MemberLoginVo vo);

    MemberEntity login(SocialUser socialUser) throws Exception;
}


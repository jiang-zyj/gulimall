package com.zyj.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyj.common.utils.HttpUtils;
import com.zyj.common.utils.PageUtils;
import com.zyj.common.utils.Query;
import com.zyj.gulimall.member.dao.MemberDao;
import com.zyj.gulimall.member.dao.MemberLevelDao;
import com.zyj.gulimall.member.entity.MemberEntity;
import com.zyj.gulimall.member.entity.MemberLevelEntity;
import com.zyj.gulimall.member.exception.PhoneExistException;
import com.zyj.gulimall.member.exception.UsernameExistException;
import com.zyj.gulimall.member.service.MemberService;
import com.zyj.gulimall.member.vo.MemberLoginVo;
import com.zyj.gulimall.member.vo.MemberRegisterVo;
import com.zyj.gulimall.member.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(MemberRegisterVo vo) {

        MemberEntity entity = new MemberEntity();

        // 设置默认等级
        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        entity.setLevelId(levelEntity.getId());

        // 设置基本信息
        // 检查用户名和手机号是否唯一。为了让controller感知异常，使用异常机制
        checkPhoneUnique(vo.getPhone());
        checkUsernameUnique(vo.getUserName());

        entity.setMobile(vo.getPhone());
        entity.setUsername(vo.getUserName());

        // 其他的默认信息
        entity.setNickname(vo.getUserName());

        // 密码加密
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());
        entity.setPassword(encode);

        this.baseMapper.insert(entity);

    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count > 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUsernameUnique(String username) throws UsernameExistException {
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (count > 0) {
            throw new UsernameExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {

        String loginAcct = vo.getLoginAcct();
        String password = vo.getPassword();

        // 1. 去数据库查询
        MemberEntity entity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginAcct).
                or().eq("mobile", loginAcct));
        if (entity == null) {
            // 登录失败
            return null;
        } else {
            // 1. 获取到数据库的password
            String passwordDb = entity.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            // 2. 密码匹配
            boolean matches = passwordEncoder.matches(password, passwordDb);
            if (matches) {
                return entity;
            } else {
                return null;
            }
        }
    }

    @Override
    public MemberEntity login(SocialUser socialUser) throws Exception {
        // 登录和注册合并逻辑
        String uid = socialUser.getUid();
        // 1. 判断当前社交用户是否已经登录过系统
        MemberDao memberDao = this.baseMapper;
        MemberEntity entity = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", socialUser.getUid()));
        if (entity != null) {
            // 这个用户已经注册
            // 更新令牌时间
            MemberEntity update = new MemberEntity();
            update.setId(entity.getId());
            update.setAccessToken(socialUser.getAccess_token());
            update.setExpiresIn(socialUser.getExpires_in());

            memberDao.updateById(update);
            entity.setAccessToken(socialUser.getAccess_token());
            entity.setExpiresIn(socialUser.getExpires_in());
            return entity;
        } else {
            // 2. 没有查到当前社交用户对应的记录，我们就需要注册这个用户
            MemberEntity register = new MemberEntity();
            // 3. 查询当前社交用户的社交账号信息（昵称、性别等）
            try {
                Map<String, String> query = new HashMap<>();
                query.put("access_token", socialUser.getAccess_token());
                query.put("uid", socialUser.getUid());
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<String, String>(), query);
                if (response.getStatusLine().getStatusCode() == 200) {
                    // 查询成功
                    String json = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSON.parseObject(json);
                    // 昵称
                    String name = jsonObject.getString("name");
                    String gender = jsonObject.getString("gender");
                    // ......
                    register.setNickname(name);
                    register.setGender("m".equals(gender)? 1 : 0);
                    // ......
                }
            } catch (Exception e) {
            }
            register.setSocialUid(socialUser.getUid());
            register.setAccessToken(socialUser.getAccess_token());
            register.setExpiresIn(socialUser.getExpires_in());

            memberDao.insert(register);
            return register;
        }
    }
}
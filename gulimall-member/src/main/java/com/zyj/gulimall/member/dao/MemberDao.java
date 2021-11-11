package com.zyj.gulimall.member.dao;

import com.zyj.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author Jiang
 * @email 2412305937@qq.com
 * @date 2021-11-01 22:22:48
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}

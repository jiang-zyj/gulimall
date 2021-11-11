package com.zyj.gulimall.order.dao;

import com.zyj.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author Jiang
 * @email 2412305937@qq.com
 * @date 2021-11-01 22:31:58
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}

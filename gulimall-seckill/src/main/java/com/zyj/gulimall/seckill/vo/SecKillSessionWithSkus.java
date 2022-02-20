package com.zyj.gulimall.seckill.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @program: gulimall
 * @ClassName SecKillSessionWithSkus
 * @author: YaJun
 * @Date: 2022 - 02 - 13 - 22:25
 * @Package: com.zyj.gulimall.seckill.vo
 * @Description:
 */
@Data
public class SecKillSessionWithSkus {

    private Long id;
    /**
     * 场次名称
     */
    private String name;
    /**
     * 每日开始时间
     */
    private Date startTime;
    /**
     * 每日结束时间
     */
    private Date endTime;
    /**
     * 启用状态
     */
    private Integer status;
    /**
     * 创建时间
     */
    private Date createTime;

    private List<SecKillSkuVo> relationSkus;
}

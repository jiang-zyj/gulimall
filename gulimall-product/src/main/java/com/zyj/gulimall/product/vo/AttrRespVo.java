package com.zyj.gulimall.product.vo;

import lombok.Data;

/**
 * @program: gulimall
 * @ClassName AttrRespVo
 * @author: YaJun
 * @Date: 2021 - 11 - 14 - 23:02
 * @Package: com.zyj.gulimall.product.vo
 * @Description:
 */
@Data
public class AttrRespVo extends AttrVo{

    /**
     * "catelogName": "手机/数码/手机" //所属分类名字
     * "groupName": "主体" // 所属分组名字
     */

    private String catelogName;
    private String groupName;

    private Long[] catelogPath;

}

package com.zyj.gulimall.search.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * @program: gulimall
 * @ClassName SearchParam
 * @author: YaJun
 * @Date: 2021 - 12 - 08 - 21:50
 * @Package: com.zyj.gulimall.search.vo
 * @Description: 封装页面所有可能传递过来的查询条件
 * <p>
 * catelog3Id=225&keyword=小米&sort=saleCount_desc&hasStock=0/1&brandId=1&brandId=2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SearchParam {

    /**
     * 页面传递过来的全文匹配关键字
     */
    private String keyword;

    /**
     * 三级分类id
     */
    private Long catalog3Id;

    /**
     * 排序条件
     * sort=saleCount_desc/asc
     * sort=hotScore_desc/asc
     * sort=skuPrice_desc/asc
     */
    private String sort;

    /**
     * 好多的过滤条件
     * hasStock(是否有货)、skuPrice区间、brandId、catelog3Id、attrs
     * hasStock=0/1
     * skuPrice=1_500/_500/500_
     * brandId=1
     * attrs=2_5寸:6寸
     */

    /**
     * 是否只显示有货
     * 0 : 无库存
     * 1 : 有库存
     */
    private Integer hasStock;

    /**
     * 价格区间查询
     */
    private String skuPrice;

    /**
     * 按照品牌id查询 可以多选
     */
    private List<Long> brandId;

    /**
     * 按照属性进行筛选
     */
    private List<String> attrs;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 原生的所有查询条件
     */
    private String _queryString;

}

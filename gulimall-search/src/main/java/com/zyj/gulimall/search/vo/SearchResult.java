package com.zyj.gulimall.search.vo;

import com.zyj.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: gulimall
 * @ClassName SearchResult
 * @author: YaJun
 * @Date: 2021 - 12 - 08 - 22:19
 * @Package: com.zyj.gulimall.search.vo
 * @Description:
 */
@Data
public class SearchResult {

    /**
     * 查询到的所有商品信息
     */
    private List<SkuEsModel> products;

    /**
     * 以下三个是分页信息
     */

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 导航页码
     */
    private List<Integer> pageNavs;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页码
     */
    private Integer totalPages;


    /**
     * 当前查询到的结果，所有涉及到的所有品牌
     */
    private List<BrandVo> brands;

    /**
     * 当前查询到的结果，所有涉及到的所有分类
     */
    private List<CatalogVo> catalogs;

    /**
     * 当前查询到的结果，所有涉及到的所有属性
     */
    private List<AttrVo> attrs;


    //====================以上是返回给页面的所有信息====================

    /**
     * 面包屑导航数据
     * 给一个初始值，防止属性为空时，navs也为空
     */
    private List<NavVo> navs = new ArrayList<>();

    /**
     * 作用：给页面返回面包屑中包含的attrId
     */
    private List<Long> attrIds = new ArrayList<>();

    @Data
    public static class NavVo {
        // 名字
        private String navName;
        // 值
        private String navValue;
        // 导航
        private String link;
    }


    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }


}

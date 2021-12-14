package com.zyj.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zyj.common.to.es.SkuEsModel;
import com.zyj.common.utils.R;
import com.zyj.gulimall.search.config.GulimallElasticsearchConfig;
import com.zyj.gulimall.search.constant.EsConstant;
import com.zyj.gulimall.search.feign.ProductFeignService;
import com.zyj.gulimall.search.service.MallSearchService;
import com.zyj.gulimall.search.vo.AttrResponseVo;
import com.zyj.gulimall.search.vo.BrandVo;
import com.zyj.gulimall.search.vo.SearchParam;
import com.zyj.gulimall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: gulimall
 * @ClassName MallSearchServiceImpl
 * @author: YaJun
 * @Date: 2021 - 12 - 08 - 21:51
 * @Package: com.zyj.gulimall.search.service.impl
 * @Description:
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam searchParam) {
        // 1. 动态构建出查询需要使用的DSL语句

        SearchResult result = null;

        // 1. 准备检索请求
        SearchRequest searchRequest = buildSearchRequest(searchParam);

        try {
            // 2. 执行检索请求
            SearchResponse response = client.search(searchRequest, GulimallElasticsearchConfig.COMMON_OPTIONS);

            // 3. 分析响应数据封装成我们需要的格式
            result = buildSearchResponse(response, searchParam);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 构建结果数据
     *
     * @return
     */
    private SearchResult buildSearchResponse(SearchResponse response, SearchParam searchParam) {

        SearchResult searchResult = new SearchResult();

        // 1. 返回的所有查询到的商品
        if (response.getHits().getHits() != null && response.getHits().getHits().length > 0) {
            List<SkuEsModel> products = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                // 设置高亮
                if (!StringUtils.isEmpty(searchParam.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(string);
                }

                products.add(esModel);
            }
            searchResult.setProducts(products);
        }


        // 2. 当前所有商品涉及到的所有的品牌信息
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        List<SearchResult.BrandVo> brandVos = brand_agg.getBuckets().stream().map(bucket -> {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            // 2.1 获取品牌id
            brandVo.setBrandId(bucket.getKeyAsNumber().longValue());
            // 2.2 获取品牌名
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("brand_name_agg");
            String brandName = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            // 2.3 获取品牌图片
            ParsedStringTerms brand_img_agg = bucket.getAggregations().get("brand_img_agg");
            String brandImg = brand_img_agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);
            brandVo.setBrandImg(brandImg);
            return brandVo;
        }).collect(Collectors.toList());
        searchResult.setBrands(brandVos);

        // 3. 当前所有商品涉及到的所有的分类信息
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        for (Terms.Bucket bucket : catalog_agg.getBuckets()) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            // 3.1 获取分类id
            long catalogId = bucket.getKeyAsNumber().longValue();
            catalogVo.setCatalogId(catalogId);
            // 3.2 获取分类名
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            catalogVos.add(catalogVo);
        }

        searchResult.setCatalogs(catalogVos);

        // 4. 当前所有商品涉及到的所有的属性信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();

            // 4.1 获取属性id
            long attrId = bucket.getKeyAsNumber().longValue();
            attrVo.setAttrId(attrId);
            // 4.2 获取属性名
            ParsedStringTerms attr_name_agg = bucket.getAggregations().get("attr_name_agg");
            String attrName = attr_name_agg.getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attrName);

            // 4.3 获取属性值（可能有多个）
            ParsedStringTerms attr_value_agg = bucket.getAggregations().get("attr_value_agg");
            List<String> attrValues = attr_value_agg.getBuckets().stream().map(item -> {
                String attrValue = item.getKeyAsString();
                return attrValue;
            }).collect(Collectors.toList());
            attrVo.setAttrValue(attrValues);

            attrVos.add(attrVo);
        }

        searchResult.setAttrs(attrVos);

        // =============以上数据从聚合信息中获取到=============

        // 5. 分页信息-页码
        searchResult.setPageNum(searchParam.getPageNum());

        // 6. 分页信息-总记录数
        long value = response.getHits().getTotalHits().value;
        searchResult.setTotal(value);

        // 7. 分页信息-总页码 计算：11 / 5 = 2 ... 1
        int totalPages = (int) (value / EsConstant.PRODUCT_PAGESIZE == 0 ? (value / EsConstant.PRODUCT_PAGESIZE) : (value / EsConstant.PRODUCT_PAGESIZE + 1));
        searchResult.setTotalPages(totalPages);

        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }

        // 8. 构建面包屑导航数据（根据属性构建）
        if (searchParam.getAttrs() != null && searchParam.getAttrs().size() > 0) {
            List<SearchResult.NavVo> navVos = searchParam.getAttrs().stream().map(attr -> {
                // 8.1 分析每个attrs传过来的查询参数值
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                // attrs=2_5寸:6寸
                String[] split = attr.split("_");
                navVo.setNavValue(split[1]);

                R r = productFeignService.attrInfo(Long.parseLong(split[0]));
                // 4.4 设置结果已经包含的所有属性id
                searchResult.getAttrIds().add(Long.parseLong(split[0]));
                if (r.getCode() == 0) {
                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(data.getAttrName());
                } else {
                    navVo.setNavName(split[0]);
                }

                // 8.2 取消了这个面包屑以后，我们要跳转到哪个地方，将请求地址的url里面的当前条件置空
                // 拿到所有的attr查询条件，去掉当前的
                String replace = replaceQueryString(searchParam, attr, "attrs");
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);

                return navVo;
            }).collect(Collectors.toList());

            searchResult.setNavs(navVos);
        }


        // 9. 构建面包屑导航数据（根据品牌构建）
        if (searchParam.getBrandId() != null && searchParam.getBrandId().size() > 0) {
            List<SearchResult.NavVo> navs = searchResult.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();

            navVo.setNavName("品牌");
            // TODO: 远程查询所有品牌
            R r = productFeignService.infos(searchParam.getBrandId());
            if (r.getCode() == 0) {
                List<BrandVo> brands = r.getData("brands", new TypeReference<List<BrandVo>>() {
                });
                StringBuffer buffer = new StringBuffer();
                String replace = "";
                for (BrandVo brandVo : brands) {
                    buffer.append(brandVo.getName()).append(":");
                    replace = replaceQueryString(searchParam, brandVo.getBrandId().toString(), "brandId");
                }
                navVo.setNavValue(buffer.toString());
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);
            }

            navs.add(navVo);
        }

        // TODO: 分类，不需要导航取消
        // 10. 构建面包屑导航数据（根据分类构建）

        searchResult.setPageNavs(pageNavs);

        return searchResult;
    }

    private String replaceQueryString(SearchParam searchParam, String value, String key) {
        String encode = null;
        try {
            encode = URLEncoder.encode(value, "UTF-8");
            // 浏览器对空格编码和java不一样
            encode = encode.replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String replace = searchParam.get_queryString().replace("&" + key + "=" + encode, "");
        return replace;
    }

    /**
     * 准备检索请求
     * # 模糊匹配，过滤（按照属性，分类，品牌，价格区间，库存），排序、分页、高亮、聚合
     *
     * @param searchParam
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam searchParam) {
        // 构建DSL语句的请求
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        /**
         * 查询：模糊匹配，过滤（按照属性，分类，品牌，价格区间，库存）
         */
        // 1. 构建 query - bool
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 1.1 query - bool - must - 模糊匹配
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", searchParam.getKeyword()));
        }

        // 1.2 query - bool - filter - 过滤 - 按照三级分类id查询
        if (searchParam.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", searchParam.getCatalog3Id()));
        }

        // 1.3 query - bool - filter - 过滤 - 按照库存是否有来进行查询
        if (searchParam.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", searchParam.getHasStock() == 1));
        }

        // 1.4 query - bool - filter - 过滤 - 按照品牌id查询
        if (searchParam.getBrandId() != null && searchParam.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", searchParam.getBrandId()));
        }

        // 1.5 query - bool - filter - 过滤 - 按照价格区间
        if (!StringUtils.isEmpty(searchParam.getSkuPrice())) {
            // _500 / 100_500 / 500_
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] split = searchParam.getSkuPrice().split("_");
            if (split.length == 2) {
                // 区间内
                boolQuery.filter(rangeQuery.gte(split[0]).lte(split[1]));
            } else if (split.length == 1) {
                if (searchParam.getSkuPrice().startsWith("_")) {
                    // 小于
                    boolQuery.filter(rangeQuery.lte(split[0]));
                } else if (searchParam.getSkuPrice().endsWith("_")) {
                    // 大于
                    boolQuery.filter(rangeQuery.gte(split[0]));
                }
            }

        }

        // 1.6 query - bool - filter - 过滤 - 按照所有指定的属性进行查询
        if (searchParam.getAttrs() != null && searchParam.getAttrs().size() > 0) {
            for (String attr : searchParam.getAttrs()) {
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                // attrs=1_5寸:8寸&attrs=2_16G:8G
                // attrs=1_5寸:8寸
                String[] split = attr.split("_");
                // 检索属性的id - attrId
                String attrId = split[0];
                // 检索属性的值 - attrValue
                String[] attrValue = split[1].split(":");
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValue));
                // 每一个必须都得生成一个nested查询
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }


        // 把以前的所有条件都拿来进行封装
        sourceBuilder.query(boolQuery);

        /**
         * 排序、分页、高亮
         */
        // 2.1 排序
        if (!StringUtils.isEmpty(searchParam.getSort())) {
            String sort = searchParam.getSort();
            // sort=saleCount_desc/asc
            String[] split = sort.split("_");
            SortOrder order = split[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort(split[0], order);
        }

        // 2.2 分页
        // pageNum:1  from:0  size:4
        sourceBuilder.from((searchParam.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        // 2.3 高亮
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle");
            builder.preTags("<b style='color:red'>");
            builder.postTags("</b>");
            sourceBuilder.highlighter(builder);
        }

        /**
         * 聚合
         */

        // 1. 聚合品牌信息 brand_agg
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg").field("brandId").size(50);

        // 品牌聚合的子聚合
        TermsAggregationBuilder brand_name_agg = AggregationBuilders.terms("brand_name_agg").field("brandName").size(1);
        TermsAggregationBuilder brand_img_agg = AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1);
        brand_agg.subAggregation(brand_name_agg);
        brand_agg.subAggregation(brand_img_agg);
        // TODO: 1. 聚合brand

        sourceBuilder.aggregation(brand_agg);

        // 2. 聚合分类信息 catalog_agg
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        TermsAggregationBuilder catalog_name_agg = AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1);
        catalog_agg.subAggregation(catalog_name_agg);
        // TODO: 2. 聚合catalog
        sourceBuilder.aggregation(catalog_agg);

        // 3. 聚合属性信息 attr_agg
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        // 聚合出当前所有的attrId
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(50);
        // 聚合出当前所有的attrId对应的名字 attr_name
        TermsAggregationBuilder attr_name_agg = AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1);
        // 聚合出当前所有的attrId对应的所有的属性值 attr_value
        TermsAggregationBuilder attr_value_agg = AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50);
        attr_id_agg.subAggregation(attr_name_agg);
        attr_id_agg.subAggregation(attr_value_agg);
        attr_agg.subAggregation(attr_id_agg);
        // TODO: 3. 聚合attr
        sourceBuilder.aggregation(attr_agg);

        String s = sourceBuilder.toString();
        System.out.println("构建的DSL：" + s);
        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);
        return searchRequest;
    }
}

package com.zyj.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.zyj.gulimall.search.config.GulimallElasticsearchConfig;
import lombok.Data;
import lombok.ToString;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class GulimallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;


    /**
     * (1). 浪费空间：{
     *          skuId: 1
     *          spuId: 11
     *          skuTitle: 华为xxx
     *          price: 998
     *          attrs: [
     *              {尺寸: 5寸},
     *              {CPU: 高通985},
     *              {分辨率: 全高清}
     *          ]
     *          ...
     *      }
     * 这种方法会有冗余
     * 100万 * 20 = 1000000 * 2KB = 2000MB = 2G, 一根内存条搞定，就算是20G，那也可以接受
     *
     * (2). 节省空间
     *      sku索引 {
     *          skuId: 1
     *          spuId: 11
     *          ...
     *      }
     *
     *      attr索引：{
     *          spuId: 11,
     *          attrs: [
     *              {尺寸: 5寸},
     *              {CPU: 高通985},
     *              {分辨率: 全高清}
     *          ]
     *      }
     *  搜索 小米：粮食、手机、电器。
     *  10000个，4000个spu
     *  分布：4000个spu对应的所有可能属性：
     *  esClient：spuId：[4000个spuId] 4000 * 8 = 32000byte = 32KB
     *  一个请求需要32KB
     *  如果在百万并发情况下：
     *  32KB * 10000 = 32000MB = 32GB
     *  一个请求就要32GB，那肯定网络都堵死了。
     *  所以，最后我们使用空间换时间，ES消耗的内存大点没事，不能丢失客服
     */

    @ToString
    @Data
    static class Account {
            private int account_number;
            private int balance;
            private String firstname;
            private String lastname;
            private int age;
            private String gender;
            private String address;
            private String employer;
            private String email;
            private String city;
            private String state;
    }

    @Test
    public void searchData() throws Exception {
        // 1. 创建检索请求
        SearchRequest searchRequest = new SearchRequest();
        // 指定索引
        searchRequest.indices("bank");
        // SearchSourceBuilder sourceBuilder 封装的条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 1.1 构造检索条件
        //sourceBuilder.query();
        //sourceBuilder.from();
        //sourceBuilder.size();
        //sourceBuilder.aggregation();

        sourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));

        // 1.2 按照年龄的值分布进行聚合
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        sourceBuilder.aggregation(ageAgg);

        // 1.3 计算平均薪资
        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        sourceBuilder.aggregation(balanceAvg);

        System.out.println("检索条件" + sourceBuilder.toString());

        // 指定DSL，即检索条件
        searchRequest.source(sourceBuilder);

        // 2. 执行检索
        SearchResponse searchResponse = client.search(searchRequest, GulimallElasticsearchConfig.COMMON_OPTIONS);

        // 3. 分析结果 searchResponse
        System.out.println(searchResponse.toString());
        // 3.1 获取所有查到的数据
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            //hit.getIndex();
            //hit.getType();
            //hit.getId();
            String string = hit.getSourceAsString();
            Account account = JSON.parseObject(string, Account.class);
            System.out.println("account" + account);
        }
        // 3.2 获取分析数据
        Aggregations aggregations = searchResponse.getAggregations();
        Terms ageAgg1 = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAgg1.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            System.out.println("年龄：" + keyAsString + "===>" + bucket.getDocCount());
        }
        Avg balanceAvg1 = aggregations.get("balanceAvg");
        System.out.println("平均薪资：" + balanceAvg1.getValue());
    }


    /**
     * 测试存储数据到es
     * 更新也可以
     *
     * @throws Exception
     */
    @Test
    public void indexData() throws Exception {
        IndexRequest request = new IndexRequest("users");
        request.id("1001");    // 数据的id
        //request.source("userName", "zhangsan", "age", "18");
        User user = new User();
        user.setUserName("zhangsan");
        user.setGender("女");
        user.setAge(18);
        String jsonString = JSON.toJSONString(user);
        request.source(jsonString, XContentType.JSON);    // 要保存的内容

        // 执行操作
        IndexResponse response = client.index(request, GulimallElasticsearchConfig.COMMON_OPTIONS);

        // 提取有用的响应数据
        System.out.println(response);
    }

    @Data
    class User {
        private String userName;
        private String gender;
        private Integer age;
    }

    @Test
    public void contextLoads() {
        System.out.println(client);
    }


}

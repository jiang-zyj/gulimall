package com.zyj.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.zyj.common.to.es.SkuEsModel;
import com.zyj.gulimall.search.config.GulimallElasticsearchConfig;
import com.zyj.gulimall.search.constant.EsConstant;
import com.zyj.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: gulimall
 * @ClassName ProductSaveServiceImpl
 * @author: YaJun
 * @Date: 2021 - 11 - 29 - 23:16
 * @Package: com.zyj.gulimall.search.service.impl
 * @Description:
 */
@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {

        // 保存到es
        // 1. 给es中建立索引product,且建立好映射关系

        // 2. 给es中保存这些数据
        // 批量操作
        // BulkRequest bulkRequest, RequestOptions options
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel model : skuEsModels) {
            // 1. 构造保存请求
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);

            indexRequest.id(model.getSpuId().toString());
            String s = JSON.toJSONString(model);
            indexRequest.source(s, XContentType.JSON);

            bulkRequest.add(indexRequest);
        }
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, GulimallElasticsearchConfig.COMMON_OPTIONS);

        // 响应结果的相关处理
        // TODO: 如果批量操作发生错误
        boolean b = bulkResponse.hasFailures();
        List<String> collect = Arrays.stream(bulkResponse.getItems()).map(item -> {
            return item.getId();
        }).collect(Collectors.toList());
        log.info("商品上架成功: {}", collect);

        return b;
    }
}

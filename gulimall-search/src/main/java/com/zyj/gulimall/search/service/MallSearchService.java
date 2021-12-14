package com.zyj.gulimall.search.service;

import com.zyj.gulimall.search.vo.SearchParam;
import com.zyj.gulimall.search.vo.SearchResult;

/**
 * @program: gulimall
 * @ClassName MallSearchService
 * @author: YaJun
 * @Date: 2021 - 12 - 08 - 21:51
 * @Package: com.zyj.gulimall.search.service
 * @Description:
 */
public interface MallSearchService {

    /**
     *
     * @param searchParam 检索的所有参数
     * @return 返回检索的结果，里面包含页面需要的所有信息
     */
    SearchResult search(SearchParam searchParam);

}

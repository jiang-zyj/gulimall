package com.zyj.gulimall.search.controller;

import com.zyj.gulimall.search.service.MallSearchService;
import com.zyj.gulimall.search.vo.SearchParam;
import com.zyj.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @program: gulimall
 * @ClassName SearchController
 * @author: YaJun
 * @Date: 2021 - 12 - 08 - 21:06
 * @Package: com.zyj.gulimall.search.controller
 * @Description:
 */
@Controller
public class SearchController {

    @Autowired
    private MallSearchService mallSearchService;

    /**
     * 自动将页面提交过来的所有请求参数封装成指定的对象
     * @param searchParam
     * @return
     */
    @GetMapping("/list.html")
    public String listPage(SearchParam searchParam, Model model, HttpServletRequest request) {

        searchParam.set_queryString(request.getQueryString());
        // 1. 根据传递来的页面的查询参数，去es中检索商品
        SearchResult result = mallSearchService.search(searchParam);

        model.addAttribute("result", result);

        return "list";
    }

}

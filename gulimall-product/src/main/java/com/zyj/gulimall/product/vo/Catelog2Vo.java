package com.zyj.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @program: gulimall
 * @ClassName Catelog2Vo
 * @author: YaJun
 * @Date: 2021 - 11 - 30 - 22:05
 * @Package: com.zyj.gulimall.product.vo
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Catelog2Vo {

    /**
     * 1级父分类id
     */
    private String catalog1Id;

    /**
     * 3级子分类
     */
    private List<Catelog3Vo> catalog3List;

    private String id;

    private String name;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Catelog3Vo {

        /**
         * 父分类，2级分类id
         */
        private String catalog2Id;

        private String id;

        private String name;
    }

}

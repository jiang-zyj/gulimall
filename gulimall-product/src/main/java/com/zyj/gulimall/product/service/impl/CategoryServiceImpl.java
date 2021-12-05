package com.zyj.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyj.common.utils.PageUtils;
import com.zyj.common.utils.Query;
import com.zyj.gulimall.product.dao.CategoryDao;
import com.zyj.gulimall.product.entity.CategoryEntity;
import com.zyj.gulimall.product.service.CategoryBrandRelationService;
import com.zyj.gulimall.product.service.CategoryService;
import com.zyj.gulimall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 递归查找所有菜单的子菜单
     *
     * @return
     */
    @Override
    public List<CategoryEntity> listWithTree() {
        // 1. 查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);


        // 2. 组装成父子的树形结构

        // 2.1 找到所有的一级分类
        List<CategoryEntity> level1Menu = entities.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == 0
        ).map((menu) -> {
            // 1. 映射
            menu.setChildren(getChildren(menu, entities));
            return menu;
        }).sorted((menu1, menu2) -> {
            // 2. 排序
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return level1Menu;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO：1. 检查当前删除的菜单，是否被别的地方引用

        // 逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    // [2, 25, 225]
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();

        List<Long> parentPath = findParentPath(catelogId, paths);

        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有关联的数据
     * @param category
     */
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    @Override
    public List<CategoryEntity> getLevel1Categories() {
        long l = System.currentTimeMillis();
        List<CategoryEntity> entities = this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        System.out.println("消耗时间：" + (System.currentTimeMillis() - l));
        return entities;
    }

    /**
     * TODO: 产生堆外内存溢出：OutOfDirectMemoryError
     * 自己的并没有产生这种问题，但是还是可能存在的
     * 产生原因：
     * 1. springboot2.0以后默认使用lettuce作为操作redis的客户端，它使用netty进行网络通信。
     * 2. lettuce的bug导致netty堆外内存溢出. -Xmx300m; netty如果没有指定堆外内存，则默认使用-Xmx300m
     * 3. 如果内存没有得到释放，则会抛堆外内存溢出异常，可以通过 -Dio.netty.maxDirectMemory 进行设置
     * 解决方案：不能使用 -Dio.netty.maxDirectMemory 只是调大堆外内存，它只会延迟出现，但不会永久不出现
     * 1. 升级lettuce客户端  2. 切换使用jedis
     *
     * @return
     */
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        // 给缓存中方json字符串，拿出来的json字符串还要转为相应的实体类 【序列化与反序列化】

        // 1. 加入缓存逻辑，缓存中的数据为json字符串
        // JSON跨语言，跨平台兼容
        String catelogJson = redisTemplate.opsForValue().get("catelogJson");
        if (StringUtils.isEmpty(catelogJson)) {
            // 2. 缓存中没有，查询数据库
            Map<String, List<Catelog2Vo>> catelogJsonFromDB = getCatelogJsonFromDB();
            // 3. 查到的数据再放入缓存，将对象转为Json放入缓存中
            String s = JSON.toJSONString(catelogJsonFromDB);
            redisTemplate.opsForValue().set("catelogJson", s);
            return catelogJsonFromDB;
        }

        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catelogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {});
        return result;
    }

    /**
     * 从数据库查询并封装分类数据
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDB() {

        /**
         * 将数据库的多次查询变为一次
         */
        List<CategoryEntity> selectList = this.baseMapper.selectList(null);

        // 1. 查出所有 1 级分类
        List<CategoryEntity> level1Categories = getParent_cid(selectList, 0L);

        // 2. 封装数据
        Map<String, List<Catelog2Vo>> collect = level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 1. 查出这个一级分类下的所有二级分类，封装成vo
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());
            // 2. 封装上面的结果
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    // 1. 查出这个二级分类下的所有三级分类，封装成vo
                    List<CategoryEntity> level3Categories = getParent_cid(selectList, l2.getCatId());
                    if (level3Categories != null) {
                        // 2. 封装成指定格式
                        List<Catelog2Vo.Catelog3Vo> l3Categories = level3Categories.stream().map(l3 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(l3Categories);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));

        return collect;
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parent_cid) {
        //return this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
        return collect;
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        // 收集当前的节点路径
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            // 递归查询父节点
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;
    }

    /**
     * 获取当前菜单的所有子菜单
     *
     * @param root
     * @param all
     * @return
     */
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map(menu -> {
            // 1. 找到子菜单
            menu.setChildren(getChildren(menu, all));
            return menu;
        }).sorted((menu1, menu2) -> {
            // 2. 菜单的排序
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }

}
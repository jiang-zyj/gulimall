package com.zyj.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyj.common.constant.ProductConstant;
import com.zyj.common.utils.PageUtils;
import com.zyj.common.utils.Query;
import com.zyj.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.zyj.gulimall.product.dao.AttrDao;
import com.zyj.gulimall.product.dao.AttrGroupDao;
import com.zyj.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.zyj.gulimall.product.entity.AttrEntity;
import com.zyj.gulimall.product.entity.AttrGroupEntity;
import com.zyj.gulimall.product.service.AttrGroupService;
import com.zyj.gulimall.product.service.AttrService;
import com.zyj.gulimall.product.vo.AttrGroupWithAttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrAttrgroupRelationDao relationDao;

    @Autowired
    private AttrDao attrDao;

    @Autowired
    private AttrService attrService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, long catelogId) {
        // 前端在查询时会多带上一个key，这个key是用来进行多字段匹配的，即哪个字段的值中有key的值，则需要查询出来
        String key = (String) params.get("key");
        // 按三级分类查
        // select * from pms_attr_group where catelog_id = ? and (catelog_id = key or attr_group_name like %key%)
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        if (!StringUtils.isEmpty(key)) {    // 如果 key 不为空，则继续构造 wrapper
            wrapper.and((obj) -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }
        if (catelogId == 0) {
            // 查询所有
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper
            );
            return new PageUtils(page);
        } else {
            wrapper.eq("catelog_id", catelogId);

            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper
            );
            return new PageUtils(page);
        }

    }

    /**
     * 获取当前分组没有关联的属性
     * @param params
     * @param attrgroupId
     * @return
     */
    @Override
    public PageUtils getNoRelation(Map<String, Object> params, Long attrgroupId) {
        // 1. 当前分组只能关联自己所属分类下的所有属性
        // 1.1 首先获取 AttrGroupEntity，通过AttrGroupEntity来获取分类id
        AttrGroupEntity attrGroupEntity = this.baseMapper.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();

        // 2. 当前分组只能关联其他分组没有关联的属性
        // 2.1 查询出当前分类下的其他分组
        List<AttrGroupEntity> otherAttrGroupEntities = this.baseMapper.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));

        // 2.2 再找到这些分组关联的属性
        List<Long> attrGroupIds = otherAttrGroupEntities.stream().map(item -> {
            return item.getAttrGroupId();
        }).collect(Collectors.toList());
        List<AttrAttrgroupRelationEntity> entities = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", attrGroupIds));
        List<Long> attrIds = entities.stream().map(item -> {
            return item.getAttrId();
        }).collect(Collectors.toList());
        // 2.3 从当前分类的所有属性中移除这些属性
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if (attrIds != null && attrIds.size() > 0) {
            queryWrapper.notIn("attr_id", attrIds);
        }
        // 模糊查询
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((wrapper) -> {
                wrapper.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = attrService.page(new Query<AttrEntity>().getPage(params), queryWrapper);

        PageUtils pageUtils = new PageUtils(page);

        return pageUtils;
    }

    /**
     * 根据分类id查询出所有的分组以及组里所有的属性
     * @param catelogId
     * @return
     */
    @Override
    public List<AttrGroupWithAttrVo> getAttrGroupWithAttrByCatelogId(Long catelogId) {

        // 1. 查询分组信息
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));

        // 2. 查询所有属性
        List<AttrGroupWithAttrVo> collect = attrGroupEntities.stream().map((attrGroupEntity) -> {
            AttrGroupWithAttrVo attrVo = new AttrGroupWithAttrVo();
            BeanUtils.copyProperties(attrGroupEntity, attrVo);
            List<AttrEntity> attrs = attrService.getRelationAttr(attrGroupEntity.getAttrGroupId());
            attrVo.setAttrs(attrs);
            return attrVo;
        }).collect(Collectors.toList());

        return collect;

    }

}
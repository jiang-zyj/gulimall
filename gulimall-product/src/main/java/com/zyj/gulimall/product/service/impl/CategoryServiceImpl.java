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
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redisson;

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
     * @CacheEvict: 失效模式
     * 同时进行多种缓存操作
     *  1. @Caching
     *  2. 指定删除某个分区下的所有数据 @CacheEvict(value = {"category", "product"}, allEntries = true)
     *  3. 约定：存储同一类型的数据，都可以指定成同一个分区。分区名默认就是缓存的前缀
     *
     * @param category
     */

    //@Caching(evict = {
    //        @CacheEvict(value = {"category", "product"}, key = "'getLevel1Categories'"),
    //        @CacheEvict(value = {"category", "product"}, key = "'getCatelogJson'")
    //})
    @CacheEvict(value = {"category", "product"}, allEntries = true)
    //@CachePut   // 双写模式
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    /**
     * 1. 每一个需要缓存的数据，我们都要指定要放到那个名字的缓存【缓存的分区(按照业务类型分)】
     * 2. @Cacheable({"category", "product"})
     *      代表当前方法的结果需要缓存，如果缓存中有，方法不用调用。
     *      如果缓存中没有，会调用方法，最后将方法的结果放入缓存中
     * 3. 默认行为
     *      1. 如果缓存中有，方法不用调用
     *      2. key默认自动生成；缓存的名字::SimpleKey [](自主生成的key值)
     *      3. 缓存的value的值。默认使用jdk序列化机制，将序列化后的数据存到redis
     *      4. 默认ttl时间：-1
     *     自定义：
     *     1. 指定生成的缓存使用自定义key   使用key属性，接收SpEL
     *     2. 指定缓存数据的存活时间
     *     3. 将数据保存为json格式
     * 4. Spring-Cache的不足：
     *      1. 读模式：
     *          缓存穿透：查询一个null数据。解决：缓存空数据 spring.cache.redis.cache-null-values=true
     *          缓存击穿：大量并发进来同时查询一个正好过期的数据。解决：加锁；默认是无加锁的; sync = true (加锁，解决击穿问题，加的是本地锁)
     *          缓存雪崩：大量的key同时过期。解决，加随机时间，加上过期时间。：spring.cache.redis.time-to-live=360000
     *      2. 写模式：(缓存与数据库一致)
     *          1. 读写加锁。
     *          2. 引入Canal，感知到MySQL的更新去更新缓存
     *          3. 读多写多，直接去数据库查询就行
     *      总结：
     *          常规数据（读多写少，即时性，一致性要求不高的数据）；完全可以使用Spring-Cache；写模式(只要缓存的数据有过期时间就足够了)
     *          特殊数据：特殊设计
     * 原理：
     *      CacheManager(RedisCacheManager) -> Cache(RedisCache) -> Cache(RedisCache)负责缓存的读写
     *
     */
    @Cacheable(value = {"category"}, key = "#root.method.name", sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categories() {
        System.out.println("getLevel1Categories...");
        long l = System.currentTimeMillis();
        List<CategoryEntity> entities = this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        System.out.println("消耗时间：" + (System.currentTimeMillis() - l));
        return entities;
    }

    @Cacheable(value = {"category"}, key = "#root.methodName")
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        System.out.println("查询了数据库");

        List<CategoryEntity> selectList = this.baseMapper.selectList(null);
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
    //@Override
    public Map<String, List<Catelog2Vo>> getCatelogJson2() {
        // 给缓存中方json字符串，拿出来的json字符串还要转为相应的实体类 【序列化与反序列化】

        /**
         * 1. 空结果缓存：解决缓存穿透
         * 2. 设置过期时间（随机值时间）：解决缓存雪崩
         * 3. 加锁：解决缓存击穿
         */

        // 1. 加入缓存逻辑，缓存中的数据为json字符串
        // JSON跨语言，跨平台兼容
        String catelogJson = redisTemplate.opsForValue().get("catelogJson");
        if (StringUtils.isEmpty(catelogJson)) {
            // 2. 缓存中没有，查询数据库
            // 保证数据库查询完成以后，将数据放在redis中，这是一个原子操作
            System.out.println("缓存不命中......将要查询数据库");
            Map<String, List<Catelog2Vo>> catelogJsonFromDB = getCatelogJsonFromDBWithRedissonLock();
            // 3. 查到的数据再放入缓存，将对象转为Json放入缓存中
            String s = JSON.toJSONString(catelogJsonFromDB);
            redisTemplate.opsForValue().set("catelogJson", s, 1, TimeUnit.DAYS);
            return catelogJsonFromDB;
        }
        System.out.println("缓存命中......直接返回");
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catelogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
        return result;
    }

    /**
     * 缓存里面的数据如何和数据库保持一致
     * 双写模式：数据库写完，写数据库的数据到redis
     * 失效模式：数据库写完，删除redis中的对应数据
     * 无论是哪种模式，都会产生缓存不一致问题
     * 但是一般来说，放入缓存的都是不会经常变的，什么0.几s一次，几秒一次啊，不是很快就变的（如果经常变，那还不如直接读数据库，放啥缓存，就算需要放缓存，那两种方法都会进行加锁，那还不如就锁数据库）
     * 如果业务可以容忍脏数据（即缓存一致性要求不高），那么可以使用读写锁，一般来说，写的次数是远不及读的次数的，所以加上这个读写锁，对性能影响不是很大
     * 也可以使用canal订阅binlog（二进制文件）的方式（canal是阿里开发的，模拟数据库的从库，数据库一发生变化，就会通过binlog（二进制文件）被canal读取到，然后canal去更新到redis中，
     * 但是多了一个中间件，也多了一些配置，这种方法的好处就是配置好后，就不用关心缓存的事情了）
     * 总结：
     *  1. 我们能放入缓存的数据本就不应该是实时的，一致性要求超高的，所以缓存数据的时候加上过期时间，
     *      保证每天拿到的当前的最新数据即可。
     *  2. 我们不应该过度设计，增加系统的复杂性
     *  3. 遇到实时性、一致性要求高的数据，就应该查询数据库，即使慢点。
     *
     * 最终针对于本系统，得出缓存一致性解决方案：
     *  1. 缓存的所有数据都有过期时间，数据过期后，下一次查询触发主动更新
     *  2. 读写数据的时候，加上分布式的读写锁
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDBWithRedissonLock() {

        // 1. 锁的名字。锁的粒度，粒度越细，速度越快。
        // 锁的粒度：具体缓存的是某个数据，11-号商品： product-11-lock product-12-lock
        RLock lock = redisson.getLock("catelogJson-lock");
        lock.lock();

        Map<String, List<Catelog2Vo>> dataFromDb;
        try {
            dataFromDb = getDataFromDb();
        } finally {
            lock.unlock();
        }

        return dataFromDb;
    }


    /**
     * 从数据库查询并封装分类数据
     *
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDBWithRedisLock() {

        // 1. 占分布式锁。去redis占坑
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("获取分布式锁成功......");
            // 加锁成功... 执行业务
            // 设置过期时间，必须和加锁是同步的，原子的
            redisTemplate.expire("lock", 30, TimeUnit.SECONDS);

            // 删除锁，先获取值进行对比 + 对比成功删除 = 原子操作；使用 rua 脚本进行解锁
            //String lockValue = redisTemplate.opsForValue().get("lock");
            //if (uuid.equalsIgnoreCase(lockValue)) {
            //    // 删除我自己的锁
            //    redisTemplate.delete("lock");
            //}
            Map<String, List<Catelog2Vo>> dataFromDb;
            try {
                dataFromDb = getDataFromDb();
            } finally {
                String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                        "then\n" +
                        "    return redis.call(\"del\",KEYS[1])\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";
                // 删除锁
                Long lock1 = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                        Arrays.asList("lock"), uuid);
            }

            return dataFromDb;
        } else {
            System.out.println("获取分布式锁失败......等待重试");
            // 加锁失败...重试。与synchronized ()一样，自旋
            // 休眠100ms重试
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatelogJsonFromDBWithRedisLock();
        }
    }

    private Map<String, List<Catelog2Vo>> getDataFromDb() {
        String catelogJson = redisTemplate.opsForValue().get("catelogJson");
        if (!StringUtils.isEmpty(catelogJson)) {
            // 缓存不为null直接返回
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catelogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return result;
        }
        System.out.println("查询了数据库");

        List<CategoryEntity> selectList = this.baseMapper.selectList(null);


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

        // 3. 查到的数据再放入缓存，将对象转为json放在缓存中
        String s = JSON.toJSONString(collect);
        redisTemplate.opsForValue().set("catelogJson", s, 1, TimeUnit.DAYS);
        return collect;
    }

    /**
     * 从数据库查询并封装分类数据
     *
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDBWithLocalLock() {

        // 只要是同一把锁，就能锁住需要这个锁的所有线程
        // 1. synchronized (this): SpringBoot 所有的组件在容器中都是单例的。
        // TODO: 本地锁：synchronized，JUC（lock）锁不住所有的线程。在分布式情况下，想要锁住所有，必须使用分布式锁
        synchronized (this) {
            // 得到锁以后，我们应该再去缓存中确认一次，如果没有才需要继续查询
            return getDataFromDb();
        }


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
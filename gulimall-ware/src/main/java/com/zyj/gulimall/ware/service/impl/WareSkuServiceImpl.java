package com.zyj.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyj.common.exception.NoStockException;
import com.zyj.common.to.mq.OrderTo;
import com.zyj.common.to.mq.StockDetailTo;
import com.zyj.common.to.mq.StockLockedTo;
import com.zyj.common.utils.PageUtils;
import com.zyj.common.utils.Query;
import com.zyj.common.utils.R;
import com.zyj.gulimall.ware.dao.WareSkuDao;
import com.zyj.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.zyj.gulimall.ware.entity.WareOrderTaskEntity;
import com.zyj.gulimall.ware.entity.WareSkuEntity;
import com.zyj.gulimall.ware.feign.OrderFeignService;
import com.zyj.gulimall.ware.feign.ProductFeignService;
import com.zyj.gulimall.ware.service.WareOrderTaskDetailService;
import com.zyj.gulimall.ware.service.WareOrderTaskService;
import com.zyj.gulimall.ware.service.WareSkuService;
import com.zyj.gulimall.ware.vo.OrderItemVo;
import com.zyj.gulimall.ware.vo.OrderVo;
import com.zyj.gulimall.ware.vo.SkuHasStockVo;
import com.zyj.gulimall.ware.vo.WareSkuLockVo;
import lombok.Data;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private WareOrderTaskService orderTaskService;

    @Autowired
    private WareOrderTaskDetailService orderTaskDetailService;

    @Autowired
    private OrderFeignService orderFeignService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 1. 库存自动解锁：
     * 下订单成功、订单过期没有支付被系统自动解锁、用户手动取消订单。都要解锁库存
     * 2. 库存锁定失败：
     * <p>
     * 需要设置为手动ack，只要解锁库存的消息失败，一定要告诉服务器解锁失败
     */


    private void unLockStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {
        // 库存解锁
        this.baseMapper.unLockStock(skuId, wareId, num);
        // 更新库存工作单的状态
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(taskDetailId);
        // 更新为已解锁
        entity.setLockStatus(2);
        orderTaskDetailService.updateById(entity);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        /**
         * skuId:
         * wareId:
         */

        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            wrapper.eq("sku_id", skuId);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        // 先判断如果还没有这个库存记录,则新增
        List<WareSkuEntity> wareSkuEntities = this.baseMapper.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (wareSkuEntities == null || wareSkuEntities.size() == 0) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStockLocked(0);
            // TODO: 远程查询sku名字，如果失败，整个事务无需回滚
            // 1. 自己 catch 异常
            // TODO: 还可以用什么办法让异常出现以后不回滚呢？高级部分解决
            try {
                R info = productFeignService.info(skuId);
                if (info.getCode() == 0) {
                    Map<String, Object> skuInfo = (Map<String, Object>) info.get("skuInfo");
                    wareSkuEntity.setSkuName((String) skuInfo.get("skuName"));
                }
            } catch (Exception e) {

            }
            this.baseMapper.insert(wareSkuEntity);
        } else {
            // 有,则为更新操作
            this.baseMapper.addStock(skuId, wareId, skuNum);
        }
    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {

        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();

            // 查询当前sku的总库存量
            // SELECT SUM(stock - stock_locked) FROM `wms_ware_sku` WHERE sku_id = 1
            Long count = baseMapper.getSkuStock(skuId);

            vo.setSkuId(skuId);
            vo.setHasStock(count != null && count > 0);
            return vo;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 为某个订单锁定库存
     * (rollbackFor = NoStockException.class)
     * 默认是运行时异常都会回滚
     * <p>
     * 库存解锁的场景：
     * 1. 下订单成功、订单过期没有支付被系统自动解锁、用户手动取消订单。都要解锁库存
     * 2. 下订单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚
     * 之前锁定的库存就要自动解锁。
     *
     * @param vo
     * @return
     */
    @Transactional(rollbackFor = NoStockException.class)
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {

        /**
         * 保存库存工作单
         * 保存库存工作单详情
         * 为了追溯（手动回滚）
         */
        WareOrderTaskEntity orderTaskEntity = new WareOrderTaskEntity();
        orderTaskEntity.setOrderSn(vo.getOrderSn());
        orderTaskService.save(orderTaskEntity);

        // 1. 原：按照下单的收货地址，找到一个就近仓库，锁定库存，但是这里暂时不这么实现了，因为数据库也没设计这些字段

        // 1. 找到每个商品在哪个仓库中有库存
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            // 查询这个商品在哪些仓库有库存
            List<Long> wareIds = this.baseMapper.listWareIdHasSkuStock(skuId);
            stock.setWareIds(wareIds);
            return stock;
        }).collect(Collectors.toList());

        // 2. 锁定库存
        // 所有商品的全局锁定标志位
        Boolean allLock = true;
        for (SkuWareHasStock hasStock : collect) {
            // 当前商品的锁定标志位
            Boolean skuStocked = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareIds();
            if (wareIds == null || wareIds.size() == 0) {
                // 没有任何仓库有这个商品的库存
                throw new NoStockException(skuId);
            }
            // 1. 如果每一个商品都锁定成功，将当前商品锁定了几件的工作单记录发送给MQ
            // 2. 锁定失败，前面保存的工作单信息就回滚了。发送出去的消息即使要解锁记录，由于去数据库查不到id，所以就不用解锁
            // 1-2-1    2-1-2 3-1-1(x)
            for (Long wareId : wareIds) {
                // 不成功返回0，成功返回其他数字
                Long count = this.baseMapper.lockSkuStock(skuId, wareId, hasStock.getNum());
                if (count == 0) {
                    // 当前仓库锁定失败，重试下一个仓库
                } else {
                    // 锁定成功
                    skuStocked = true;
                    // TODO: 锁定成功，告诉MQ锁定成功，发消息
                    WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity(null, skuId, "", hasStock.getNum(), orderTaskEntity.getId(), wareId, 1);
                    orderTaskDetailService.save(entity);
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(entity, stockDetailTo);
                    StockLockedTo lockedTo = new StockLockedTo();
                    lockedTo.setId(orderTaskEntity.getId());
                    // 只发id不行，防止回滚以后找不到数据
                    lockedTo.setDetail(stockDetailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", lockedTo);
                    break;
                }
            }
            if (!skuStocked) {
                // 当前商品所有仓库都没有锁住
                throw new NoStockException(skuId);
            }
        }
        // 3. 肯定全部都是锁定成功的
        return true;
    }

    @Override
    public void unLockStock(StockLockedTo to) {
        StockDetailTo detail = to.getDetail();
        Long detailId = detail.getId();
        // 解锁
        // 1. 查询数据库关于这个订单的锁定库存信息。
        // 有：证明库存锁定成功了
        //      解锁：订单情况。
        //          1. 没有这个订单，必须解锁
        //          2. 有这个订单，继续判断订单状态
        //              订单状态：已取消：解锁库存
        //                       没取消：不能解锁
        // 没有：库存锁定失败了，库存回滚了，这种情况无需解锁
        WareOrderTaskDetailEntity byId = orderTaskDetailService.getById(detailId);
        if (byId != null) {
            // 解锁
            Long id = to.getId();
            WareOrderTaskEntity taskEntity = orderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn();
            // 根据订单号查询订单状态
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode() == 0) {
                OrderVo data = r.getData(new TypeReference<OrderVo>() {
                });
                // 如果订单不存在 || 订单状态为取消状态
                if (data == null || data.getStatus() == 4) {
                    // 当前库存工作单详情，状态为1（已锁定）时才能进行解锁，其他状态不能进行解锁
                    if (byId.getLockStatus() == 1) {
                        // 解锁库存
                        unLockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detailId);
                    }
                }
            } else {
                // 消息拒绝后重新将消息放回队列中，让别人继续消费
                throw new RuntimeException("远程服务调用失败");
            }
        } else {
            // 无需解锁
        }
    }

    /**
     * 防止订单服务卡顿，导致订单消息状态消息一直改不了，库存消息优先到期。查订单状态的新建状态，什么都不做就走了。
     * 防止卡顿的订单，永远不能解锁库存
     * @param orderTo
     */
    @Transactional
    @Override
    public void unLockStock(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        // 查一下最新库存解锁的状态，防止重复解锁库存
        WareOrderTaskEntity task = orderTaskService.getOrderTaskByOrderSn(orderSn);
        Long id = task.getId();
        // 按照工作单找到所有没有解锁的库存，进行解锁
        List<WareOrderTaskDetailEntity> list = orderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>()
                .eq("task_id", id)
                .eq("lock_status", 1));
        // Long skuId, Long wareId, Integer num, Long taskDetailId
        for (WareOrderTaskDetailEntity entity : list) {
            unLockStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum(), entity.getId());
        }
    }

    @Data
    class SkuWareHasStock {
        /**
         * 商品skuId
         */
        private Long skuId;
        /**
         * 锁定件数
         */
        private Integer num;
        /**
         * 仓库id
         */
        private List<Long> wareIds;
    }

}
package com.zyj.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyj.common.exception.NoStockException;
import com.zyj.common.to.mq.OrderTo;
import com.zyj.common.utils.PageUtils;
import com.zyj.common.utils.Query;
import com.zyj.common.utils.R;
import com.zyj.common.vo.MemberRespVo;
import com.zyj.gulimall.order.constant.OrderConstant;
import com.zyj.gulimall.order.dao.OrderDao;
import com.zyj.gulimall.order.entity.OrderEntity;
import com.zyj.gulimall.order.entity.OrderItemEntity;
import com.zyj.gulimall.order.enume.OrderStatusEnum;
import com.zyj.gulimall.order.feign.CartFeignService;
import com.zyj.gulimall.order.feign.MemberFeignService;
import com.zyj.gulimall.order.feign.ProductFeignService;
import com.zyj.gulimall.order.feign.WmsFeignService;
import com.zyj.gulimall.order.interceptor.LoginUserInterceptor;
import com.zyj.gulimall.order.service.OrderItemService;
import com.zyj.gulimall.order.service.OrderService;
import com.zyj.gulimall.order.to.OrderCreateTo;
import com.zyj.gulimall.order.vo.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private final ThreadLocal<OrderSubmitVo> submitVoThreadLocal = new ThreadLocal<>();

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private CartFeignService cartFeignService;

    @Autowired
    private WmsFeignService wmsFeignService;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Autowired
    private StringRedisTemplate redisTemplate;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();

        OrderConfirmVo confirmVo = new OrderConfirmVo();
        System.out.println("主线程..." + Thread.currentThread().getId());

        // 获取之前的请求，从主线程拿到数据，在副线程中设置数据，这样request就不会为空
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            // 1. 远程查询，获取收货地址信息
            System.out.println("member线程..." + Thread.currentThread().getId());
            // 每个请求都来共享之前的请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(memberRespVo.getId());
            confirmVo.setAddress(address);
        }, executor);


        CompletableFuture<Void> getCartItemFuture = CompletableFuture.runAsync(() -> {
            // 2. 远程查询购物车所有选中的购物项
            System.out.println("cart线程..." + Thread.currentThread().getId());
            // 每个请求都来共享之前的请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems();
            confirmVo.setItems(items);
            // feign在远程调用之前要构造请求，调用很多的拦截器
            // RequestInterceptor interceptor
        }, executor).thenRunAsync(() -> {
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> collect = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            R hasStock = wmsFeignService.getSkusHasStock(collect);
            List<SkuStockVo> data = hasStock.getData(new TypeReference<List<SkuStockVo>>() {
            });
            if (data != null) {
                Map<Long, Boolean> map = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                confirmVo.setStocks(map);
            }
        }, executor);


        // 3. 查询用户积分
        Integer integration = memberRespVo.getIntegration();
        confirmVo.setIntegration(integration);

        // 4. 其他数据自动计算

        // TODO: 5. 防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId(), token, 30, TimeUnit.MINUTES);
        confirmVo.setOrderToken(token);

        CompletableFuture.allOf(getAddressFuture, getCartItemFuture).get();
        return confirmVo;
    }

    @Transactional(timeout = 30)    // a事务的所有设置就传播到了和他共用一个事务的方法
    public void a() {
        // 事务的一个坑：如果方法都是在同一个对象中，那么这里的b，c做任何设置都没用。都是和a共用一个事务
        // 因为在这里调用的话，相当于直接把b，c这两个方法写在a方法中，这样事务就是默认使用a的事务了
        // 同一个对象内事务方法互调默认失效，原因：绕过来代理对象
        // 事务是使用代理对象来控制的
        //this.b(); 没用
        //this.c(); 没用

        OrderServiceImpl orderService = (OrderServiceImpl) AopContext.currentProxy();
        orderService.b();    // a事务
        orderService.c();    // 新事务
        //int i = 10 / 0; // a、b回滚，c不回滚
    }

    @Transactional(propagation = Propagation.REQUIRED, timeout = 2)
    public void b() {
        // 7s
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void c() {
    }


    /**
     * 本地事务，在分布式系统中，只能控制住自己的回滚，控制不了其他服务的回滚
     * 本地事务在分布式系统中可能产生的问题：
     * 1. 远程服务假失败：
     * 远程服务其实成功了，由于网络故障等没有返回
     * 导致：订单回滚，库存却扣减
     * 2. 远程服务执行完成，下面的其他方法出现问题
     * 导致：已执行的远程请求，肯定不能回滚
     * 分布式事务：最大原因。网络问题 + 分布式机器
     *
     * @param vo
     * @return
     */
    //@GlobalTransactional    // 高并发场景不适用，因为XA模式会占用很多锁
    // 使用柔性事务-可靠消息+最终一致性方案（异步确保型）
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        // 将订单提交数据放到ThreadLocal中，方便后续方法使用
        submitVoThreadLocal.set(vo);
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        responseVo.setCode(0);
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        // 去验令牌、
        // 1. 验证令牌【令牌的对比和删除必须保证原子性】
        String orderToken = vo.getOrderToken();
        // 返回0表示脚本失败，1表示执行成功
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        // 原子验证令牌和删除令牌
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId()), orderToken);
        // 1. 创建订单、订单项等信息
        OrderCreateTo order = createOrder();
        // 2. 验价
        BigDecimal payAmount = order.getOrder().getPayAmount();
        BigDecimal payPrice = vo.getPayPrice();
        if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
            // 金额对比
            // ...
            // TODO：3. 保存订单
            saveOrder(order);
            // 4. 库存锁定。只要有异常，回滚订单数据。
            // 订单号、所有订单项（skuId、skuName、num）
            // 远程锁库存
            WareSkuLockVo lockVo = new WareSkuLockVo();
            lockVo.setOrderSn(order.getOrder().getOrderSn());
            List<OrderItemVo> locks = order.getOrderItems().stream().map(item -> {
                OrderItemVo itemVo = new OrderItemVo();
                itemVo.setSkuId(item.getSkuId());
                itemVo.setTitle(item.getSkuName());
                itemVo.setCount(item.getSkuQuantity());
                return itemVo;
            }).collect(Collectors.toList());
            lockVo.setLocks(locks);
            // TODO: 4. 远程锁库存
            // 库存成功了，但是网络超时了，订单回滚，库存不回滚
            // 为了保证高并发。库存服务自己回滚。可以发消息给库存服务
            // 库存服务本身也可以使用自动解锁模式    消息队列
            R r = wmsFeignService.orderLockStock(lockVo);
            if (r.getCode() == 0) {
                // 锁定成功了
                responseVo.setOrder(order.getOrder());

                // TODO: 远程扣减积分
                // 订单回滚、库存不回滚
                //int i = 10 / 0;
                // TODO：订单创建成功，发送消息给MQ
                rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());
                return responseVo;
            } else {
                // 锁定失败
                String msg = (String) r.get("msg");
                throw new NoStockException(msg);
                //responseVo.setCode(3);
                //return responseVo;
            }

        } else {
            // 令牌验证失败；code = 2 表示验价失败
            responseVo.setCode(2);
            return responseVo;
        }

        //String redisToken = redisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId());
        //if (orderToken != null && orderToken.equals(redisToken)) {
        //    // 令牌验证通过
        //    redisTemplate.delete(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId());
        //} else {
        //    // 不通过
        //    responseVo.setCode(1);
        //    return responseVo;
        //}
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        OrderEntity order_sn = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        return order_sn;
    }

    @Override
    public void closeOrder(OrderEntity orderEntity) {
        // 查询当前这个订单的最新状态
        OrderEntity entity = this.getById(orderEntity.getId());
        // 订单的状态只有新建状态才能进行关单
        if (entity.getStatus().equals(OrderStatusEnum.CREATE_NEW.getCode())) {
            // 关单
            OrderEntity update = new OrderEntity();
            update.setId(entity.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(update);
            // 关单成功，主动发给交换机一个关单成功的消息
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(entity, orderTo);
            try {
                // TODO: 保证消息一定会发送出去，每一个消息都可以做好日志记录（给数据库保存每一个消息的详细信息）。
                // TODO: 定期扫描数据库，将失败的消息再发送一遍
                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
            } catch (Exception e) {
                // TODO: 将没发送成功的消息进行重试发送。
            }
        }
    }

    /**
     * 保存订单数据
     *
     * @param order
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        // 设置修改时间
        orderEntity.setModifyTime(new Date());
        // 保存订单
        this.save(orderEntity);
        // 保存所有订单项
        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }

    /**
     * 生成订单返回数据
     *
     * @return
     */
    private OrderCreateTo createOrder() {
        OrderCreateTo createTo = new OrderCreateTo();
        // 1. 构建订单实体
        // 生成一个订单号
        String orderSn = IdWorker.getTimeId();
        OrderEntity orderEntity = buildOrderEntity(orderSn);

        // 2. 获取到所有的订单项
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);

        // 3. 封装Order对象的计算价格、积分等相关信息
        computePrice(orderEntity, orderItemEntities);

        createTo.setOrder(orderEntity);
        createTo.setOrderItems(orderItemEntities);


        return createTo;
    }

    /**
     * 封装Order对象的计算价格、积分等相关信息
     *
     * @param orderEntity
     * @param orderItemEntities
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        BigDecimal total = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal giftGrowth = new BigDecimal("0.0");
        BigDecimal giftIntegration = new BigDecimal("0.0");

        // 订单的总额，叠加每一个订单项的总额信息
        for (OrderItemEntity entity : orderItemEntities) {
            total = total.add(entity.getRealAmount());
            promotion = promotion.add(new BigDecimal(entity.getPromotionAmount().toString()));
            coupon = coupon.add(new BigDecimal(entity.getCouponAmount().toString()));
            integration = integration.add(new BigDecimal(entity.getIntegrationAmount().toString()));
            giftGrowth = giftGrowth.add(new BigDecimal(entity.getGiftGrowth().toString()));
            giftIntegration = giftIntegration.add(new BigDecimal(entity.getGiftIntegration().toString()));
        }
        // 1. 订单价格相关
        orderEntity.setTotalAmount(total);
        // 设置总额信息
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setCouponAmount(coupon);
        orderEntity.setIntegrationAmount(integration);

        // 设置积分信息
        orderEntity.setIntegration(giftIntegration.intValue());
        orderEntity.setGrowth(giftGrowth.intValue());

        // 设置删除状态 0表示未删除
        orderEntity.setDeleteStatus(0);

    }

    /**
     * 构建订单实体类
     *
     * @return
     */
    private OrderEntity buildOrderEntity(String orderSn) {
        MemberRespVo respVo = LoginUserInterceptor.loginUser.get();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);
        // 设置会员id
        orderEntity.setMemberId(respVo.getId());
        orderEntity.setMemberUsername(respVo.getUsername());
        // 获取收货地址信息
        OrderSubmitVo submitVo = submitVoThreadLocal.get();
        R fare = wmsFeignService.getFare(submitVo.getAddrId());
        FareVo fareResp = fare.getData(new TypeReference<FareVo>() {
        });
        // 设置运费信息
        orderEntity.setFreightAmount(fareResp.getFare());
        // 设置收货人信息
        orderEntity.setReceiverCity(fareResp.getAddress().getCity());
        orderEntity.setReceiverDetailAddress(fareResp.getAddress().getDetailAddress());
        orderEntity.setReceiverName(fareResp.getAddress().getName());
        orderEntity.setReceiverPhone(fareResp.getAddress().getPhone());
        orderEntity.setReceiverProvince(fareResp.getAddress().getProvince());
        orderEntity.setReceiverPostCode(fareResp.getAddress().getPostCode());
        orderEntity.setReceiverRegion(fareResp.getAddress().getRegion());

        //
        // 设置订单状态信息
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        // 设置自动确认时间(天)
        orderEntity.setAutoConfirmDay(7);
        orderEntity.setConfirmStatus(0);

        return orderEntity;
    }

    /**
     * 构建所有订单项数据
     *
     * @param orderSn
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        // 最后确定每个购物项的价格
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if (currentUserCartItems != null && currentUserCartItems.size() > 0) {
            List<OrderItemEntity> itemEntities = currentUserCartItems.stream().map(cartItem -> {
                OrderItemEntity itemEntity = buildOrderItem(cartItem);
                itemEntity.setOrderSn(orderSn);
                return itemEntity;
            }).collect(Collectors.toList());
            return itemEntities;
        }
        return null;
    }

    /**
     * 构建某一个订单项信息
     *
     * @param cartItem
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity itemEntity = new OrderItemEntity();
        // 1. 订单信息：订单号 √
        // 2. 商品的SPU信息
        Long skuId = cartItem.getSkuId();
        R r = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo data = r.getData(new TypeReference<SpuInfoVo>() {
        });
        itemEntity.setSpuId(data.getId());
        itemEntity.setSpuBrand(data.getBrandId().toString());
        itemEntity.setSpuName(data.getSpuName());
        itemEntity.setCategoryId(data.getCatalogId());

        // 3. 商品的SKU信息
        itemEntity.setSkuId(cartItem.getSkuId());
        itemEntity.setSkuName(cartItem.getTitle());
        itemEntity.setSkuPic(cartItem.getImage());
        itemEntity.setSkuPrice(cartItem.getPrice());
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        itemEntity.setSkuAttrsVals(skuAttr);
        itemEntity.setSkuQuantity(cartItem.getCount());

        // 4. 优惠信息【不做】
        // 5. 积分信息
        itemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString())).intValue());
        itemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString())).intValue());
        // 6. 订单项的价格信息
        itemEntity.setPromotionAmount(new BigDecimal("0.0"));
        itemEntity.setCouponAmount(new BigDecimal("0.0"));
        itemEntity.setIntegrationAmount(new BigDecimal("0.0"));
        // 当前订单项的实际金额
        BigDecimal origin = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString()));
        BigDecimal realAmount = origin.subtract(itemEntity.getPromotionAmount()).subtract(itemEntity.getCouponAmount()).subtract(itemEntity.getIntegrationAmount());
        itemEntity.setRealAmount(realAmount);
        return itemEntity;
    }

}
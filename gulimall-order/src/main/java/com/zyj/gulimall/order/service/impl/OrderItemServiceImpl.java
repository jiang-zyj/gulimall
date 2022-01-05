package com.zyj.gulimall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbitmq.client.Channel;
import com.zyj.common.utils.PageUtils;
import com.zyj.common.utils.Query;
import com.zyj.gulimall.order.dao.OrderItemDao;
import com.zyj.gulimall.order.entity.OrderEntity;
import com.zyj.gulimall.order.entity.OrderItemEntity;
import com.zyj.gulimall.order.entity.OrderReturnReasonEntity;
import com.zyj.gulimall.order.service.OrderItemService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;


@RabbitListener(queues = {"hello-java-queue"})
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * queues: 声明需要监听的队列
     *
     * org.springframework.amqp.core.Message
     *
     * 参数可以写以下类型：
     * 1. Message message：原生消息的详细信息，头 + 体
     * 2. T<发送的消息的类型>：OrderReturnReasonEntity content
     * 3. Channel channel：当前传输信息的通道
     *
     * Queue：可以有很多人都来监听。只要收到消息，队列就会删除消息，而且只能有一个人收到此消息
     * 场景：
     *      1)、订单服务启动多个：同一个消息，只能有一个客户端收到
     *      2)、只有一个消息完全处理完，方法运行结束，我们才能收到下一个消息
     */
    //@RabbitListener(queues = {"hello-java-queue"})
    @RabbitHandler
    public void receiveMessage(Message message,
                               OrderReturnReasonEntity content,
                               Channel channel) throws InterruptedException {
        System.out.println("接收到消息：" + message + "===>内容：" + content);
        // {"id":1,"name":"哈哈","sort":null,"status":null,"createTime":1641302670010}'
        byte[] body = message.getBody();
        // 消息头属性信息
        MessageProperties properties = message.getMessageProperties();
        //Thread.sleep(3000);
        System.out.println("消息处理完成==>" + content.getName());
        // deliveryTag在channel内是按顺序自增的
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        System.out.println("deliveryTag==>" + deliveryTag);

        // 签收消息，非批量签收
        try {
            if (deliveryTag % 2 == 0) {
                // 收货
                channel.basicAck(deliveryTag, false);
                System.out.println("签收了消息..." + deliveryTag);
            } else {
                // 退货 requeue=false 丢弃  requeue=true 发回服务器，消息重新入队
                // long deliveryTag, boolean multiple, boolean requeue
                channel.basicNack(deliveryTag, false, false);
                // long deliveryTag, boolean requeue
                //channel.basicReject();
                System.out.println("没有签收消息..." + deliveryTag);
            }

        } catch (IOException e) {
            // 网络中断
            e.printStackTrace();
        }
    }

    @RabbitHandler
    public void receiveMessage2(OrderEntity content) throws InterruptedException {
        System.out.println("接收到消息内容：" + content);
    }
}
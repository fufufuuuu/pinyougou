package cn.itcast.core.service.order;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.log.PayLogDao;
import cn.itcast.core.dao.order.OrderDao;
import cn.itcast.core.dao.order.OrderItemDao;
import cn.itcast.core.pojo.cart.Cart;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.util.uniqueuekey.IdWorker;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class orderServiceImpl implements OrderService {
    @Resource
    private OrderDao orderDao;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private ItemDao itemDao;
    @Resource
    private OrderItemDao orderItemDao;
    @Resource
    private PayLogDao payLogDao;

    @Override
    public void add(Order order, String name) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("BUYER_CART").get(name);
        if (cartList != null){
            double orderPrice = 0f;// 订单总价
            List<Long> orderList = new ArrayList<>();
            for (Cart cart : cartList) {
                // 保存订单信息
                IdWorker idWorker = new IdWorker();
                long orderId = idWorker.nextId();
                orderList.add(orderId);
                order.setOrderId(orderId); // 订单id
                order.setStatus("1"); // 订单状态  1未付款
                order.setCreateTime(new Date()); // 订单创建时间 当前时间
                order.setUserId(name); // 如名
                order.setSourceType("2"); // 订单来源  2PC端
                order.setSellerId(cart.getSellerId()); // 商家Id
                // 保存订单明细
                List<OrderItem> orderItemList = cart.getOrderItemList();
                if (orderItemList != null && orderItemList.size() > 0){
                    for (OrderItem orderItem : orderItemList) {
                        orderItem.setId(idWorker.nextId()); // 明细Id
                        orderItem.setOrderId(orderId); // 订单信息外键
                        Long itemId = orderItem.getItemId();
                        Item item = itemDao.selectByPrimaryKey(itemId);
                        orderItem.setPicPath(item.getImage()); // 商品图片
                        orderItem.setGoodsId(item.getGoodsId()); // SPU_ID
                        orderItem.setTitle(item.getTitle()); // 标题
                        orderItem.setSellerId(item.getSellerId());
                        orderItem.setPrice(new BigDecimal(item.getPrice().doubleValue())); // 单价
                        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * orderItem.getNum())); // 总价
                        orderPrice += item.getPrice().doubleValue() * orderItem.getNum(); // 订单总价
                        orderItemDao.insertSelective(orderItem);
                    }
                    // 订单总额
                    order.setPayment(new BigDecimal(orderPrice));
                    orderDao.insertSelective(order);
                }
                // 创建交易日志
                PayLog payLog = new PayLog();
                payLog.setOutTradeNo(String.valueOf(idWorker.nextId()));
                payLog.setCreateTime(new Date());
                payLog.setTotalFee((long)orderPrice*100); // 订单金额 分
                payLog.setUserId(name); // 用户ID
                payLog.setTradeState("0");
                payLog.setOrderList(orderList.toString().replace("[","").replace("]", "")); // 订单号列表
                payLog.setPayType("1"); // 微信支付
                payLogDao.insertSelective(payLog);
                // 支付过程中需要修改 先放到redis
                redisTemplate.boundHashOps("payLog").put(name ,payLog);
            }
        }
        // 删除redis中的购物车
        redisTemplate.boundHashOps("BUYER_CART").delete(name);
    }
}

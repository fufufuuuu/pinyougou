package cn.itcast.core.service.order;

import cn.itcast.core.pojo.order.Order;

public interface OrderService {
    void add(Order order, String name);
}

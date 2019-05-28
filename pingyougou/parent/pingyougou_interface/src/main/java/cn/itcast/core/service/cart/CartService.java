package cn.itcast.core.service.cart;

import cn.itcast.core.pojo.cart.Cart;
import cn.itcast.core.pojo.item.Item;

import java.util.List;

public interface CartService {
    /**
     * 获取商家Id
     * @param id
     * @return
     */
    Item findOne(Long id);

    /**
     * 回显购物车 填充数据
     * @param cartList
     */
    List<Cart> autoDataToCartList(List<Cart> cartList);

    /**
     * 已登录， 将购物车存到redis中
     * @param username
     * @param cartList
     */
    void mergeCarList(String username, List<Cart> cartList);

    List<Cart> findCartListFromRedis(String name);
}

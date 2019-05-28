package cn.itcast.core.service.cart;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.cart.Cart;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.pojo.seller.Seller;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Resource
    private ItemDao itemDao;
    @Resource
    private SellerDao sellerDao;
    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 获取商家Id
     *
     * @param id
     * @return
     */
    @Override
    public Item findOne(Long id) {
        return itemDao.selectByPrimaryKey(id);
    }

    /**
     * 回显购物车 填充数据
     *
     * @param cartList
     */
    @Override
    public List<Cart> autoDataToCartList(List<Cart> cartList) {
        for (Cart cart : cartList) {
            String sellerId = cart.getSellerId();
            List<OrderItem> orderItemList = cart.getOrderItemList();
            Seller seller = sellerDao.selectByPrimaryKey(sellerId);
            // 获取商家店铺名称
            seller.setNickName(seller.getNickName());
            // 填充购物项的数据
            for (OrderItem orderItem : orderItemList) {
                Long itemId = orderItem.getItemId();
                Item item = itemDao.selectByPrimaryKey(itemId);
                orderItem.setPicPath(item.getImage()); // 图片
                orderItem.setTitle(item.getTitle());
                BigDecimal price = item.getPrice();// 标题
                orderItem.setPrice(price); // 单价
                Integer num = orderItem.getNum();
                // 商品小记
                // 商品数量 * 单价
                BigDecimal bigDecimal = new BigDecimal(price.doubleValue() * num);
                orderItem.setTotalFee(bigDecimal);
            }
        }
        return cartList;
    }

    /**
     * 已登录， 将购物车存到redis中
     *
     * @param username
     * @param newCartList
     */
    @Override
    public void mergeCarList(String username, List<Cart> newCartList) {
        // 取出老车
        List<Cart> oldCartList = (List<Cart>) redisTemplate.boundHashOps("BUYER_CART").get(username);
        // 将新车合并到老车中
        oldCartList = mergeNewCartListToOldCartList(oldCartList, newCartList);
        // 保存老车
        redisTemplate.boundHashOps("BUYER_CART").put(username, oldCartList);
    }

    /**
     * 已登录， 回显购物车
     * @param name
     * @return
     */
    @Override
    public List<Cart> findCartListFromRedis(String name) {
        return (List<Cart>) redisTemplate.boundHashOps("BUYER_CART").get(name);
    }

    // 将新车合并到老车中
    private List<Cart> mergeNewCartListToOldCartList(List<Cart> oldCartList, List<Cart> newCartList) {
        if (oldCartList != null) {
            if (newCartList != null) {
                // 都不为空才开始合并 装车
                for (Cart newCart : newCartList) {
                    int indexOf = oldCartList.indexOf(newCart);
                    // 判断是否属于同一商家
                    if (indexOf != -1) {
                        // 是 判断是否属于同一商品
                        List<OrderItem> newOrderItemList = newCart.getOrderItemList();
                        List<OrderItem> oldOrderItemList = oldCartList.get(indexOf).getOrderItemList();
                        for (OrderItem newOrderItem : newOrderItemList) {
                            int index = oldOrderItemList.indexOf(newOrderItem);
                            if (index != -1) {
                                // 获取到了值说明在购物车是同款商品 合并购买数量
                                OrderItem oldOrderItem = oldOrderItemList.get(index);
                                oldOrderItem.setNum(oldOrderItem.getNum() + newOrderItem.getNum());
                            } else {
                                // 获取不到 （== -1）不在购物车 不是同款商品
                                oldOrderItemList.add(newOrderItem);
                            }
                        }
                    } else {
                        // 不属于同一商家直接装车
                        oldCartList.add(newCart);
                    }
                }
            } else {
                // 新车为空返回老车
                return oldCartList;
            }
        } else {
            // 老车为空返回新车
            return newCartList;
        }
        // 都不为空返回合并后到老车
        return oldCartList;
    }
}

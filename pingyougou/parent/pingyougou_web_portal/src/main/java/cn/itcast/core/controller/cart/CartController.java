package cn.itcast.core.controller.cart;

import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.cart.Cart;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.service.cart.CartService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;


    /**
     * 添加商品到购物车
     * @param itemId
     * @param num
     * @param request
     * @param response
     * @return
     */
    @CrossOrigin(origins = "http://localhost:9532")
    @RequestMapping("addGoodsToCartList.do")
    public Result addGoodsToCartList(Long itemId, Integer num, HttpServletRequest request, HttpServletResponse response){
        try {
            // 1.定义一个空车集合
            List<Cart> cartList = null;
            // 2.判断本地是否有车
            Cookie[] cookies = request.getCookies();
            // 设置开关
            boolean flag = false;
            if (cookies != null && cookies.length > 0){
                // 3.有车，直接取出并赋值
                for (Cookie cookie : cookies) {
                    if ("BUYER_CART".equals(cookie.getName())){
                        String decode1 = URLDecoder.decode(cookie.getValue(), "UTF-8");
                        String decode = URLDecoder.decode(decode1, "UTF-8");
                        cartList = JSON.parseArray(decode, Cart.class);
                        flag = true;
                        break;
                    }
                }
            }
            // 4.无车，创建一个购物车
            if (cartList == null){
                cartList = new ArrayList<>();
            }
            // 5.到这里就有车了，将数据先封装到cart中
            Cart cart = new Cart();
            Item item = cartService.findOne(itemId);
            cart.setSellerId(item.getSellerId());
            cart.setSellerName(item.getSeller());
            List<OrderItem> orderItemList = new ArrayList<>();
            OrderItem orderItem = new OrderItem();
            orderItem.setItemId(itemId);
            orderItem.setNum(num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            // 6.商品装车
            // 6.1 判断商品是否属于同一个商家（商家Id一样）
            int sellerIdIndex = cartList.indexOf(cart);
            if (sellerIdIndex != -1){
                // 是同一个商家 判断是否同一个商品(skuId一样)
                List<OrderItem> oldOrderItemList = cartList.get(sellerIdIndex).getOrderItemList();
                // 获取脚标值
                int orderItemIndex = oldOrderItemList.indexOf(orderItem);
                // 不等于-1 有值则是同一个商品
                if (orderItemIndex != -1){
                    // 合并商品
                    OrderItem oldOrderItem = oldOrderItemList.get(orderItemIndex);
                    oldOrderItem.setNum(num + oldOrderItem.getNum());
                }else {
                    // 同商家不同商品 添加到对应商家的购物集中
                    oldOrderItemList.add(orderItem);
                }
            }else {
                // 不同商家 直接装车
                cartList.add(cart);
            }
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            //System.out.println("用户名"+name);
            // 判断用户是否登录 用户名是否是anonymousUser 是未登录 否已登录
            if (!"anonymousUser".equals(name)){
                // 已登录 保存到redis中
                cartService.mergeCarList(name, cartList);
                // 清空本地购物车
                if (flag){
                    Cookie cookie = new Cookie("BUYER_CART", null);
                    cookie.setMaxAge(1);
                    cookie.setPath("/");    // 设置cookie共享
                    response.addCookie(cookie);
                }
            }else {
                // 未登录状态将购物车保存到用户本地（cookie）
                String encode = URLEncoder.encode(JSON.toJSONString(cartList), "UTF-8");
                Cookie cookie = new Cookie("BUYER_CART", encode);
                // 设置cookie 过期时间 1小时
                cookie.setMaxAge(60 * 60);
                cookie.setPath("/");    // 设置cookie共享
                response.addCookie(cookie);
            }
            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
            return new Result(false, "添加失败");
    }

    /**
     * 回显购物车
     * @param request
     * @return
     */
    @RequestMapping("/findCartList.do")
    public List<Cart> findCartList (HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        // 判断用户是否登录
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        // 是否登录都要先从cookie获取 已登录就同步到redis中
        List<Cart> cartList = new ArrayList<>();
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0){
            for (Cookie cookie : cookies) {
                if ("BUYER_CART".equals(cookie.getName())){
                    String cookieValue = cookie.getValue();
                    String encode = URLDecoder.decode(cookieValue, "UTF-8");
                    System.out.println(encode);
                    cartList = JSON.parseArray(encode, Cart.class);
                    break;
                }
            }
        }
        // 已登录
        if (!"anonymousUser".equals(name)){
            // 将cookie中到购物车同步到redis
            if (cartList != null){
                cartService.mergeCarList(name, cartList);
                // 清空本地
                Cookie cookie = new Cookie("BUYER_CART", null);
                cookie.setMaxAge(0);
                cookie.setPath("/");    // 设置cookie共享
                response.addCookie(cookie);
            }
            // 从redis中读取购物车
            cartList = cartService.findCartListFromRedis(name);
        }
        if (cartList != null){
            // 填充要展示到数据
            cartList = cartService.autoDataToCartList(cartList);
        }
        return cartList;
    }
}

package cn.itcast.core.service.seller;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.seller.Seller;

public interface SellerService {
    /**
     * 商家入驻申请
     * @param seller
     */
    void add(Seller seller);

    /**
     * 待审核商家列表
     * @param page
     * @param rows
     * @param seller
     * @return
     */
    PageResult search(Integer page, Integer rows, Seller seller);

    /**
     * 商家详情
     * @param sellerId
     * @return
     */
    Seller findOne(String sellerId);


    void updateStatus(String sellerId, String status);
}

package cn.itcast.core.service.seller;

import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.pojo.seller.SellerQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.annotation.Resource;

@Service
public class SellerServiceImpl implements SellerService {
    @Resource
    private SellerDao sellerDao;
    /**
     * 商家入驻申请
     * @param seller
     */
    @Override
    public void add(Seller seller) {
        //状态 0待审核
        seller.setStatus("0");
//        密码加密
        String password = seller.getPassword();
        password = new BCryptPasswordEncoder().encode(password);
        seller.setPassword(password);
        sellerDao.insertSelective(seller);
    }

    /**
     * 待审核的商家列表
     * @param page
     * @param rows
     * @param seller
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, Seller seller) {
        PageHelper.startPage(page, rows);
        SellerQuery query = new SellerQuery();
        if (seller.getStatus() != null && "".equals(seller.getStatus())) {
            query.createCriteria().andStatusEqualTo(seller.getStatus());
        }
        Page<Seller> sellers = (Page<Seller>) sellerDao.selectByExample(query);
        return new PageResult(sellers.getTotal(),sellers.getResult());
    }

    /**
     * 商家详情
     * @param sellerId
     * @return
     */
    @Override
    public Seller findOne(String sellerId) {
        return sellerDao.selectByPrimaryKey(sellerId);
    }

    /**
     * 审核商家
     * @param sellerId
     * @param status
     */
    @Override
    public void updateStatus(String sellerId, String status) {
        Seller seller = new Seller();
        seller.setSellerId(sellerId);
        seller.setStatus(status);
        sellerDao.updateByPrimaryKeySelective(seller);
    }
}

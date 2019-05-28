package cn.itcast.core.service.addr;

import cn.itcast.core.dao.address.AddressDao;
import cn.itcast.core.pojo.address.Address;
import cn.itcast.core.pojo.address.AddressQuery;
import com.alibaba.dubbo.config.annotation.Service;

import javax.annotation.Resource;
import java.util.List;
@Service
public class AddrServiceImpl implements AddrService{
    @Resource
    private AddressDao addressDao;
    @Override
    public List<Address> findListByLoginUser(String userId) {
        AddressQuery query = new AddressQuery();
        query.createCriteria().andUserIdEqualTo(userId);
        addressDao.selectByExample(query);
        return addressDao.selectByExample(query);
    }
}

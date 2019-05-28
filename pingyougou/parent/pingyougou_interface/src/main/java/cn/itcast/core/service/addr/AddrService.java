package cn.itcast.core.service.addr;

import cn.itcast.core.pojo.address.Address;

import java.util.List;

public interface AddrService {
    List<Address> findListByLoginUser(String userId);
}

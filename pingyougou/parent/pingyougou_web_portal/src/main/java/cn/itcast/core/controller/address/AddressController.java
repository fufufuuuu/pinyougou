package cn.itcast.core.controller.address;

import cn.itcast.core.pojo.address.Address;
import cn.itcast.core.service.addr.AddrService;

import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/address")
public class AddressController {
    @Reference
    private AddrService addrService;
    @RequestMapping("/findListByLoginUser.do")
    public List<Address> findListByLoginUser(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Address> listByLoginUser = addrService.findListByLoginUser(name);
        System.out.println(listByLoginUser.toString());
        return listByLoginUser;
    }
}

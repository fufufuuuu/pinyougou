package cn.itcast.core.service.userDetail;

import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.service.seller.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Set;

public class UserDetailServiceImpl implements UserDetailsService {
    private SellerService sellerService;
    //属性注入
    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Seller seller = sellerService.findOne(username);
        //认证用户（必须是审核通过的用户 status = 1）
        if (seller != null &&  "1".equals(seller.getStatus())){
            Set<GrantedAuthority> authoritySet = new HashSet<>(); //添加权限的集合
            SimpleGrantedAuthority role_seller = new SimpleGrantedAuthority("ROLE_SELLER");
            authoritySet.add(role_seller);
            return new User(username,seller.getPassword(),authoritySet);
        }
        return null;
    }
}

package cn.itcast.core.controller.login;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 显示当前登录人
 */
@RestController
@RequestMapping("/login")
public class LoginController {
    @RequestMapping("/showName.do")
    public Map<String, String> showName(){
        Map<String, String> map = new HashMap<>();
        //获取登录信息
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        map.put("username", name);
        return map;
    }
}

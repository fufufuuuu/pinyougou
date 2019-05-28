package cn.itcast.core.controller.user;

import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.user.User;
import cn.itcast.core.service.user.UserService;
import cn.itcast.core.util.checkPhone.PhoneFormatCheckUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Reference
    private UserService userService;

    /**
     * 发送验证码
     * @param phone
     * @return
     */
    @RequestMapping("/sendCode.do")
    public Result sendCode(@RequestParam("phone") String phone){
        try {
            if (!PhoneFormatCheckUtils.isPhoneLegal(phone)){
                return new Result(false, "手机号不合法");
            }
                userService.sendCode(phone);
                return new Result(true, "发送成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "发送失败");
    }

    @RequestMapping("/add.do")
    public Result add(@RequestBody User user, String smscode){
        try {
            userService.register(user, smscode);
            return new Result(true, "注册成功");
        } catch (RuntimeException e) {
            return new Result(false, e.getMessage());
        } catch (Exception e){
            e.printStackTrace();
        }
        return new Result(false, "注册失败");
    }

}

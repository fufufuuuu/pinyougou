package cn.itcast.core.service.user;

import cn.itcast.core.pojo.user.User;

public interface UserService {
    /**
     * 发送验证码
     * @param phone
     */
    void sendCode(String phone);

    /**
     * 用户注册
     * @param user
     * @param code
     */
    void register(User user, String code);
}

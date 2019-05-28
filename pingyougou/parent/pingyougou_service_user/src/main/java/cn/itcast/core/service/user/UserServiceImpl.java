package cn.itcast.core.service.user;

import cn.itcast.core.dao.user.UserDao;
import cn.itcast.core.pojo.user.User;
import cn.itcast.core.util.md5.MD5Util;
import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.annotation.Resource;
import javax.jms.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService{
    @Resource
    private JmsTemplate jmsTemplate;
    @Resource
    private Destination smsDestination;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UserDao userDao;
    @Override
    public void sendCode(final String phone) {
        // 六位验证码
        final String code = RandomStringUtils.randomNumeric(6);
        System.out.println(code);
        // 将验证码保存到redis，并设置过期时间 5分钟
        redisTemplate.boundValueOps(phone).set(code);
        redisTemplate.boundValueOps(phone).expire(5, TimeUnit.MINUTES);

        // 发送数据到mq
        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                // map消息体
                MapMessage mapMessage = session.createMapMessage();
                mapMessage.setString("phoneNumbers", phone);
                mapMessage.setString("signName", "傅仁龙");
                mapMessage.setString("templateCode", "SMS_140720901");
                mapMessage.setString("templateParam", "{\"code\":\""+code+"\"}");
                return mapMessage;
            }
        });
    }

    @Override
    public void register(User user, String code) {
        String phoneCode = redisTemplate.boundValueOps(user.getPhone()).get().toString();
        // 判断验证码是否正确
        if (code != null && !"".equals(code) && phoneCode != null && !"".equals(phoneCode) && code.equals(phoneCode)){
            String password = user.getPassword();
            password = MD5Util.MD5Encode(password, null);
            user.setPassword(password);
            user.setUpdated(new Date());
            user.setCreated(new Date());
            // 用户保存
            userDao.insertSelective(user);
        } else {
            throw new RuntimeException("验证码不正确");
        }
    }
}

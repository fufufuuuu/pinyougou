package cn.itcast.core.listener;

import cn.itcast.core.service.staticPage.StaticPageService;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

public class PageListener implements MessageListener {
    @Resource
    StaticPageService staticPageService;
    @Override
    public void onMessage(Message message) {
        ActiveMQTextMessage activeMQMessage = (ActiveMQTextMessage) message;
        try {
            String text = activeMQMessage.getText();
            System.out.println(text);
            staticPageService.getHtml(Long.parseLong(text));

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}

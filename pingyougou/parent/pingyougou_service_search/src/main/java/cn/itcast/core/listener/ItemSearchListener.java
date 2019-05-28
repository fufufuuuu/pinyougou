package cn.itcast.core.listener;

import cn.itcast.core.service.search.ItemSearchService;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

public class ItemSearchListener implements MessageListener {
    @Resource
    ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        ActiveMQTextMessage activeMQMessage = (ActiveMQTextMessage) message;
        try {
            String text = activeMQMessage.getText();
            System.out.println(text);
            itemSearchService.saveItemToSolr(Long.parseLong(text));
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}

package cn.itcast.core.listener;
import cn.itcast.core.service.search.ItemSearchService;
import org.apache.activemq.command.ActiveMQTextMessage;
import javax.annotation.Resource;
import javax.jms.Message;
import javax.jms.MessageListener;

public class ItemDeleteListener implements MessageListener {
    @Resource
    ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        ActiveMQTextMessage activeMQMessage = (ActiveMQTextMessage) message;
        try {
            String Id = activeMQMessage.getText();
            System.out.println(Id);
            itemSearchService.deleteItemFromSolr(Long.parseLong(Id));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

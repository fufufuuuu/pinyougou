package cn.itcast.core.controller.pay;

import cn.itcast.core.entity.Result;
import cn.itcast.core.service.pay.PayService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {
    @Reference
    private PayService payService;

    /**
     * 生成二维码 统一支付接口
     * @return
     * @throws Exception
     */
    @RequestMapping("/createNative.do")
    public Map<String, String> createNative(String username) throws Exception {
        return payService.createNative(username);
    }

    /**
     * 查询支付结果 查询订单接口
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryPayStatus.do")
    public Result queryPayStatus(String out_trade_no){
        try {
            int i = 0;
            while (true){
                Map<String, String> map = payService.queryPayStatus(out_trade_no);
                String trade_state = map.get("trade_state");
                if ("SUCCESS".equals(trade_state)){
                    return new Result(true, "支付成功");
                } else {
                    Thread.sleep(5000);
                    i ++;
                }
                if (i > 360){
                    return new Result(false, "二维码超时");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "支付失败");
        }
    }
}

package cn.itcast.core.service.pay;

import cn.itcast.core.dao.log.PayLogDao;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.util.http.HttpClient;
import cn.itcast.core.util.uniqueuekey.IdWorker;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
@Service
public class PayServiceImpl implements PayService {
    @Value("${appid}")
    private String appid;
    @Value("${mch_id}")
    private String mch_id;
    @Value("${notifyurl}")
    private String notifyurl;
    @Value("${partnerkey}")
    private String partnerkey;

    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private PayLogDao payLogDao;

    /**
     * 统一下单接口
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, String> createNative(String username) throws Exception {
        // 从redis中获取交易日志
        PayLog payLog = (PayLog) redisTemplate.boundHashOps("payLog").get(username);
        // 接口地址
        String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        // 封装接口需要的参数
        Map<String, String> data = new HashMap<>();
        IdWorker idWorker = new IdWorker();
       // long out_trade_no = idWorker.nextId();
//        公众账号ID	appid	是	String(32)	wxd678efh567hg6787	微信支付分配的公众账号ID（企业号corpid即为此appId）
        data.put("appid", appid);
//        商户号	mch_id	是	String(32)	1230000109	微信支付分配的商户号
        data.put("mch_id", mch_id);
//        随机字符串	nonce_str	是	String(32)	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	随机字符串，长度要求在32位以内。推荐随机数生成算法
        data.put("nonce_str", WXPayUtil.generateNonceStr());
//        签名	sign	是	String(32)	C380BEC2BFD727A4B6845133519F3AD6	通过签名算法计算得出的签名值，详见签名生成算法
        // todo
//        商品描述	body	是	String(128)	腾讯充值中心-QQ会员充值
        data.put("body", "测试");
//        商户订单号	out_trade_no	是	String(32)	20150806125346	商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|* 且在同一个商户号下唯一。详见商户订单号
        data.put("out_trade_no", String.valueOf(payLog.getOutTradeNo()));
//        标价金额	total_fee	是	Int	88	订单总金额，单位为分，详见支付金额
        //data.put("total_fee", payLog.getTotalFee().toString()); // 上线再用
        data.put("total_fee", "1"); // 测试金额
//        终端IP	spbill_create_ip	是	String(64)	123.12.12.123	支持IPV4和IPV6两种格式的IP地址。调用微信支付API的机器IP
        data.put("spbill_create_ip", "123.12.12.123");
//        通知地址	notify_url	是	String(256)	http://www.weixin.qq.com/wxpay/pay.php	异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。
        data.put("notify_url", notifyurl);
//        交易类型	trade_type	是	String(16)	JSAPI
        data.put("trade_type", "NATIVE");
        // 将map转换成xml
        String xmlParam = WXPayUtil.generateSignedXml(data, partnerkey);
        // 模拟浏览器发送请求
        HttpClient httpClient = new HttpClient(url);
        httpClient.setXmlParam(xmlParam);
        httpClient.isHttps();
        httpClient.post();

        // 请求完成后响应结果
        String contentXML = httpClient.getContent(); // xml
        Map<String, String> map = WXPayUtil.xmlToMap(contentXML);
        map.put("total_fee", String.valueOf(payLog.getTotalFee()));    // 展示的金额
        map.put("out_trade_no", String.valueOf(payLog.getOutTradeNo()));  // 订单号
        return map;
    }

    /**
     * 查询订单接口
     * @param out_trade_no
     * @return
     */
    @Override
    public Map<String, String> queryPayStatus(String out_trade_no) throws Exception {
        // 从redis中获取交易日志
        Map<String, String> data = new HashMap<>();
        // 接口地址
        String url = "https://api.mch.weixin.qq.com/pay/orderquery";
//        公众账号ID	appid	是	String(32)	wxd678efh567hg6787	微信支付分配的公众账号ID（企业号corpid即为此appId）
        data.put("appid", appid);
//        商户号	mch_id	是	String(32)	1230000109	微信支付分配的商户号
        data.put("mch_id", mch_id);
//        微信订单号	transaction_id	二选一	String(32)	1009660380201506130728806387	微信的订单号，建议优先使用
//        商户订单号	out_trade_no	String(32)	20150806125346	商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|*@ ，且在同一个商户号下唯一。 详见商户订单号
        data.put("out_trade_no", out_trade_no);
//        随机字符串	nonce_str	是	String(32)	C380BEC2BFD727A4B6845133519F3AD6	随机字符串，不长于32位。推荐随机数生成算法
        data.put("nonce_str", WXPayUtil.generateNonceStr());
//        签名	sign	是	String(32)	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	通过签名算法计算得出的签名值，详见签名生成算法
        String signedXml = WXPayUtil.generateSignedXml(data, partnerkey);
        HttpClient httpClient = new HttpClient(url);
        httpClient.setXmlParam(signedXml);
        httpClient.isHttps();
        httpClient.post();
        String contentXML = httpClient.getContent();

        Map<String, String> map = WXPayUtil.xmlToMap(contentXML);
        if ("SUCCESS".equals(map.get("trade_state"))){
            PayLog payLog = new PayLog();
            payLog.setTradeState("1");
            payLogDao.updateByPrimaryKeySelective(payLog);
            // 删除redis日志
            //redisTemplate.boundHashOps("") 暂时没有username
        }

        return map;
    }
}

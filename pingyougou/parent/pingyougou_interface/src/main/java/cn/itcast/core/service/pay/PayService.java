package cn.itcast.core.service.pay;

import java.util.Map;

public interface PayService {
    Map<String, String> createNative(String username) throws Exception;

    Map<String, String> queryPayStatus(String out_trade_no) throws Exception;
}

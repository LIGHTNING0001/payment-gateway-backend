package com.payment.gateway.service;

import com.payment.gateway.entity.Order;
import com.payment.gateway.entity.Refund;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WechatService {

    @Value("${payment.wechat.app-id}")
    private String appId;

    @Value("${payment.wechat.mch-id}")
    private String mchId;

    @Value("${payment.wechat.api-key}")
    private String apiKey;

    @Value("${payment.wechat.notify-url}")
    private String notifyUrl;

    public String createOrder(Order order) {
        // 调用微信统一下单接口
        // TODO: 实现微信统一下单逻辑
        // 这里返回模拟的二维码
        return "weixin://wxpay/bizpayurl?pr=0000000000";
    }

    public Map<String, String> refund(Order order, Refund refund) {
        // 调用微信退款接口
        // TODO: 实现微信退款逻辑
        // 这里返回模拟的退款结果
        Map<String, String> result = new HashMap<>();
        result.put("tradeNo", "WECHAT_REFUND_" + System.currentTimeMillis());
        result.put("status", "1"); // 退款成功
        return result;
    }

    public boolean verifyNotify(Map<String, Object> params) {
        // 验签微信异步通知
        // TODO: 实现微信验签逻辑
        return true;
    }

}
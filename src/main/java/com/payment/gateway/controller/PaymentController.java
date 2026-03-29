package com.payment.gateway.controller;

import com.alibaba.fastjson.JSONObject;
import com.payment.gateway.service.PaymentService;
import com.payment.gateway.service.AlipayService;
import com.payment.gateway.service.NotifyBroadcastService;
import com.payment.gateway.service.WechatService;
import com.payment.gateway.utils.HttpClient;
import com.payment.gateway.enums.MerchantEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/pay")
@Slf4j
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private WechatService wechatService;

    @Autowired
    private NotifyBroadcastService notifyBroadcastService;


    @PostMapping("/create")
    public String createOrder(@RequestBody Map<String, Object> request) {
        // 打印请求参数
        log.info("=== 统一收单/支付接口请求参数 === \n");
        log.info(JSONObject.toJSONString(request, true));

        // 获取商户信息
        String merchantNo = request.get("merchantNo").toString();
        MerchantEnum merchantEnum = MerchantEnum.explain(merchantNo);
        if (merchantEnum == null) {
            throw new RuntimeException("商户不存在");
        }

        // 构建请求参数
        Map<String, String> paymentRequest = new java.util.HashMap<>();
        paymentRequest.put("version", request.get("version").toString());
        paymentRequest.put("format", request.get("format").toString());
        paymentRequest.put("merchantNo", merchantNo);
        paymentRequest.put("signType", merchantEnum.getSignType());
        paymentRequest.put("method", "POLYMERIZE_MAIN_SWEPTN"); // 支付宝主扫
        paymentRequest.put("signContent", JSONObject.toJSONString(request.get("signContent")));

        // 发送请求到真实的支付系统
        HttpClient httpClient = new HttpClient();
        JSONObject response = null;
        try {
            response = httpClient.post(paymentRequest, merchantEnum.getUrl() + "/api/acquiring", merchantEnum);
        } catch (Exception e) {
            throw new RuntimeException("请求支付系统失败: " + e.getMessage());
        }

        // 打印响应结果
        log.info("=== 统一收单/支付接口响应结果 === \n");
        log.info(JSONObject.toJSONString(response, true));

        // 创建订单
        Object sc = request.get("signContent");
        Map<String, Object> signContentMap;

        if (sc instanceof Map) {
            signContentMap = (Map<String, Object>) sc;
        } else {
            String s = String.valueOf(sc);
            signContentMap = JSONObject.parseObject(
                    s, new com.alibaba.fastjson.TypeReference<Map<String, Object>>() {}
            );
        }
        com.payment.gateway.entity.Order order = paymentService.createOrder(signContentMap, merchantNo);

        // 更新订单信息
        order.setQrCode(response.getString("qrCode"));
        order.setStatus(response.getString("status"));
        paymentService.updateOrder(order);

        return response.toJSONString();
    }

    @PostMapping("/query")
    public Map<String, Object> queryOrder(@RequestBody Map<String, Object> request) {
        // 打印请求参数
        log.info("=== 订单查询接口请求参数 === \n");
        log.info(JSONObject.toJSONString(request, true));

        // 获取商户信息
        String merchantNo = request.get("merchantNo").toString();
        MerchantEnum merchantEnum = MerchantEnum.explain(merchantNo);
        if (merchantEnum == null) {
            throw new RuntimeException("商户不存在");
        }

        // 构建请求参数并调用真实查询
        Map<String, String> paymentRequest = new java.util.HashMap<>();
        paymentRequest.put("version", request.get("version").toString());
        paymentRequest.put("format", request.get("format").toString());
        paymentRequest.put("merchantNo", merchantNo);
        paymentRequest.put("signType", merchantEnum.getSignType());
        paymentRequest.put("method", "POLYMERIZE_QUERY");
        paymentRequest.put("signContent", JSONObject.toJSONString(request.get("signContent")));

        HttpClient httpClient = new HttpClient();
        JSONObject response;
        try {
            response = httpClient.post(paymentRequest, merchantEnum.getUrl() + "/api/acquiring", merchantEnum);
        } catch (Exception e) {
            throw new RuntimeException("请求支付系统失败: " + e.getMessage());
        }

        // 打印响应结果
        log.info("=== 订单查询接口响应结果 === \n");
        log.info(JSONObject.toJSONString(response, true));

        return response;
    }

    @GetMapping("/notify/stream")
    public SseEmitter notifyStream() {
        log.info("客户端订阅交易通知");
        return notifyBroadcastService.subscribe();
    }

    @PostMapping("/notify")
    public String notify(@RequestBody Map<String, Object> request) {
        log.info("=== 异步通知回调接口请求参数 ===");
        log.info(com.alibaba.fastjson.JSON.toJSONString(request, true));

        // 验证通知来源
        String channel = request.get("channel").toString();
        boolean verifyResult = false;

        if (channel.equals("alipay")) {
            verifyResult = alipayService.verifyNotify(request);
        } else if (channel.equals("wechat")) {
            verifyResult = wechatService.verifyNotify(request);
        }

        if (!verifyResult) {
            // 打印响应结果
            System.out.println("=== 异步通知回调接口响应结果 ===");
            System.out.println("fail");
            return "fail";
        }

        notifyBroadcastService.broadcast(request);

        // 打印响应结果
        log.info("=== 异步通知回调接口响应结果 ===");
        log.info("success");

        return "success";
    }

}

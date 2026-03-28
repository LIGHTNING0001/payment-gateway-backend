package com.payment.gateway.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.payment.gateway.service.PaymentService;
import com.payment.gateway.service.AlipayService;
import com.payment.gateway.service.WechatService;
import com.payment.gateway.utils.RSA2Helper;
import com.payment.gateway.utils.CFCAHelper;
import com.payment.gateway.utils.HttpClient;
import com.payment.gateway.enums.MerchantEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @Value("${security.rsa2.private-key-path}")
    private String rsa2PrivateKeyPath;

    @Value("${security.rsa2.public-key-path}")
    private String rsa2PublicKeyPath;

    @Value("${security.rsa2.private-key-pwd}")
    private String rsa2PrivateKeyPwd;

    @Value("${security.cfca.private-key-path}")
    private String cfcaPrivateKeyPath;

    @Value("${security.cfca.public-key-path}")
    private String cfcaPublicKeyPath;

    @Value("${security.cfca.private-key-pwd}")
    private String cfcaPrivateKeyPwd;

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
        System.out.println("=== 订单查询接口请求参数 ===");
        System.out.println(com.alibaba.fastjson.JSON.toJSONString(request, true));

        // 验签
        String signType = request.get("signType").toString();
        String sign = request.get("sign").toString();
        String signContent = request.get("signContent").toString();

        boolean verifyResult = false;
        if (signType.equals("RSA2")) {
            verifyResult = RSA2Helper.verify(signContent, sign, rsa2PublicKeyPath);
        } else if (signType.equals("CFCA")) {
            verifyResult = CFCAHelper.verify(signContent, sign, cfcaPublicKeyPath);
        }

        if (!verifyResult) {
            throw new RuntimeException("验签失败");
        }

        // 解析signContent
        Map<String, Object> signContentMap = JSONObject.parseObject(signContent, Map.class);
        String transNo = signContentMap.get("transNo").toString();

        // 查询订单
        com.payment.gateway.entity.Order order = paymentService.queryOrder(transNo);

        // 返回结果
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("transNo", order.getTransNo());
        result.put("orderAmt", order.getOrderAmt());
        result.put("status", order.getStatus());
        result.put("tradeNo", order.getTradeNo());

        // 打印响应结果
        System.out.println("=== 订单查询接口响应结果 ===");
        System.out.println(com.alibaba.fastjson.JSON.toJSONString(result, true));

        return result;
    }

    @PostMapping("/notify")
    public String notify(@RequestBody Map<String, Object> request) {
        // 打印请求参数
        System.out.println("=== 异步通知回调接口请求参数 ===");
        System.out.println(com.alibaba.fastjson.JSON.toJSONString(request, true));

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

        // 处理通知
        String transNo = request.get("transNo").toString();
        String tradeNo = request.get("tradeNo").toString();
        String status = request.get("status").toString();

        paymentService.notify(transNo, tradeNo, status);

        // 打印响应结果
        System.out.println("=== 异步通知回调接口响应结果 ===");
        System.out.println("success");

        return "success";
    }

    @PostMapping("/precreate")
    public Map<String, Object> precreateOrder(@RequestBody Map<String, Object> request) {
        // 打印请求参数
        System.out.println("=== 预下单接口请求参数 ===");
        System.out.println(com.alibaba.fastjson.JSON.toJSONString(request, true));

        // 验签
        String signType = request.get("signType").toString();
        String sign = request.get("sign").toString();
        String signContent = request.get("signContent").toString();
        String merchantNo = request.get("merchantNo").toString();

        boolean verifyResult = false;
        if (signType.equals("RSA2")) {
            verifyResult = RSA2Helper.verify(signContent, sign, rsa2PublicKeyPath);
        } else if (signType.equals("CFCA")) {
            verifyResult = CFCAHelper.verify(signContent, sign, cfcaPublicKeyPath);
        }

        if (!verifyResult) {
            throw new RuntimeException("验签失败");
        }

        // 解析signContent
        Map<String, Object> signContentMap = JSONObject.parseObject(request.get("signContent").toString(), Map.class);

        // 生成预下单参数
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("merchantNo", merchantNo);
        result.put("signType", signType);
        result.put("signContent", signContentMap);
        result.put("sign", sign);

        // 打印响应结果
        System.out.println("=== 预下单接口响应结果 ===");
        System.out.println(com.alibaba.fastjson.JSON.toJSONString(result, true));

        return result;
    }

}
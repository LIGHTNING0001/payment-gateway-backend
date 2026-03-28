package com.payment.gateway.controller;

import com.alibaba.fastjson.JSONObject;
import com.payment.gateway.enums.MerchantEnum;
import com.payment.gateway.utils.HttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/pay/refund")
@Slf4j
public class RefundController {

    @PostMapping("")
    public Map<String, Object> createRefund(@RequestBody Map<String, Object> request) {
        log.info("=== 退款申请接口请求参数 === \n");
        log.info(JSONObject.toJSONString(request, true));

        String merchantNo = request.get("merchantNo").toString();
        MerchantEnum merchantEnum = MerchantEnum.explain(merchantNo);
        if (merchantEnum == null) {
            throw new RuntimeException("商户不存在");
        }

        Map<String, String> paymentRequest = new java.util.HashMap<>();
        paymentRequest.put("version", request.get("version").toString());
        paymentRequest.put("format", request.get("format").toString());
        paymentRequest.put("merchantNo", merchantNo);
        paymentRequest.put("signType", merchantEnum.getSignType());
        paymentRequest.put("method", "POLYMERIZE_REFUND");
        paymentRequest.put("signContent", JSONObject.toJSONString(request.get("signContent")));

        HttpClient httpClient = new HttpClient();
        JSONObject response;
        try {
            response = httpClient.post(paymentRequest, merchantEnum.getUrl() + "/api/acquiring", merchantEnum);
        } catch (Exception e) {
            throw new RuntimeException("请求支付系统失败: " + e.getMessage());
        }

        log.info("=== 退款申请接口响应结果 === \n");
        log.info(JSONObject.toJSONString(response, true));
        return response;
    }

    @PostMapping("/query")
    public Map<String, Object> queryRefund(@RequestBody Map<String, Object> request) {
        log.info("=== 退款查询接口请求参数 === \n");
        log.info(JSONObject.toJSONString(request, true));

        String merchantNo = request.get("merchantNo").toString();
        MerchantEnum merchantEnum = MerchantEnum.explain(merchantNo);
        if (merchantEnum == null) {
            throw new RuntimeException("商户不存在");
        }

        Map<String, String> paymentRequest = new java.util.HashMap<>();
        paymentRequest.put("version", request.get("version").toString());
        paymentRequest.put("format", request.get("format").toString());
        paymentRequest.put("merchantNo", merchantNo);
        paymentRequest.put("signType", merchantEnum.getSignType());
        paymentRequest.put("method", "POLYMERIZE_REFUND_QUERY");
        paymentRequest.put("signContent", JSONObject.toJSONString(request.get("signContent")));

        HttpClient httpClient = new HttpClient();
        JSONObject response;
        try {
            response = httpClient.post(paymentRequest, merchantEnum.getUrl() + "/api/acquiring", merchantEnum);
        } catch (Exception e) {
            throw new RuntimeException("请求支付系统失败: " + e.getMessage());
        }

        log.info("=== 退款查询接口响应结果 === \n");
        log.info(JSONObject.toJSONString(response, true));
        return response;
    }

}

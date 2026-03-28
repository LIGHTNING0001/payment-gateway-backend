package com.payment.gateway.controller;

import com.payment.gateway.service.RefundService;
import com.payment.gateway.utils.RSA2Helper;
import com.payment.gateway.utils.CFCAHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/pay/refund")
public class RefundController {

    @Autowired
    private RefundService refundService;

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

    @PostMapping("")
    public Map<String, Object> createRefund(@RequestBody Map<String, Object> request) {
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
        Map<String, Object> signContentMap = com.alibaba.fastjson.JSON.parseObject(signContent, Map.class);

        // 创建退款
        com.payment.gateway.entity.Refund refund = refundService.createRefund(signContentMap, merchantNo);

        // 返回结果
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("transNo", refund.getTransNo());
        result.put("origTransNo", refund.getOrigTransNo());
        result.put("orderAmt", refund.getOrderAmt());
        result.put("status", refund.getStatus());

        return result;
    }

    @PostMapping("/query")
    public Map<String, Object> queryRefund(@RequestBody Map<String, Object> request) {
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
        Map<String, Object> signContentMap = com.alibaba.fastjson.JSON.parseObject(signContent, Map.class);
        String transNo = signContentMap.get("transNo").toString();

        // 查询退款
        com.payment.gateway.entity.Refund refund = refundService.queryRefund(transNo);

        // 返回结果
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("transNo", refund.getTransNo());
        result.put("origTransNo", refund.getOrigTransNo());
        result.put("orderAmt", refund.getOrderAmt());
        result.put("status", refund.getStatus());
        result.put("tradeNo", refund.getTradeNo());

        return result;
    }

}
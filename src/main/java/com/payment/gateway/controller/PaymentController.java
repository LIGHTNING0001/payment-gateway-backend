package com.payment.gateway.controller;

import com.alibaba.fastjson.JSONObject;
import com.payment.gateway.service.PaymentService;
import com.payment.gateway.service.NotifyBroadcastService;
import com.payment.gateway.utils.HttpClient;
import com.payment.gateway.utils.RSA2Helper;
import com.payment.gateway.utils.RSAHelper;
import com.payment.gateway.enums.MerchantEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/pay")
@Slf4j
public class PaymentController {

    @Autowired
    private PaymentService paymentService;


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

    @PostMapping(value = "/notify", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> notifyJson(@RequestBody Map<String, Object> request) {
        return handleNotify(request);
    }

    @PostMapping(value = "/notify", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Map<String, Object> notifyForm(@RequestParam Map<String, String> request) {
        Map<String, Object> normalized = new HashMap<>(request);
        return handleNotify(normalized);
    }

    private Map<String, Object> handleNotify(Map<String, Object> request) {
        log.info("=== 异步通知回调接口请求参数 ===");
        log.info(JSONObject.toJSONString(request, true));

        String merchantNo = getRequiredString(request, "merchantNo");
        MerchantEnum merchantEnum = MerchantEnum.explain(merchantNo);
        if (merchantEnum == null) {
            throw new RuntimeException("商户不存在");
        }

        String merchantSignType = merchantEnum.getSignType();
        String requestSignType = stringValue(request.get("signType"));
        if (!merchantSignType.equals(requestSignType)) {
            log.warn("回调signType与商户配置不一致, merchantNo={}, requestSignType={}, merchantSignType={}",
                    merchantNo, requestSignType, merchantSignType);
        }

        String signContentRaw = getRequiredString(request, "signContent");
        Map<String, Object> signContent;
        boolean verifySuccess = true;
        if ("RSA2".equals(merchantSignType)) {
            String sign = getRequiredString(request, "sign");
            String verifyPlainText = buildNotifySignSource(request, merchantEnum, signContentRaw);
            verifySuccess = RSA2Helper.verify(verifyPlainText, sign, merchantEnum.getPublicKey());
            if (!verifySuccess) {
                throw new RuntimeException("通知验签失败");
            }
            signContent = parseSignContent(signContentRaw);
        } else if ("CFCA".equals(merchantSignType)) {
            String decryptedSignContent;
            try {
                decryptedSignContent = RSAHelper.decrypt(signContentRaw, merchantEnum.getPublicKey());
            } catch (Exception e) {
                throw new RuntimeException("CFCA回调解密失败: " + e.getMessage(), e);
            }
            signContent = parseSignContent(decryptedSignContent);
        } else {
            throw new RuntimeException("不支持的签名类型: " + merchantSignType);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("merchantNo", merchantNo);
        response.put("method", stringValue(request.get("method")));
        response.put("version", stringValue(request.get("version")));
        response.put("signType", merchantSignType);
        response.put("verifySuccess", verifySuccess);
        response.put("signContent", signContent);
        response.put("success", true);

        notifyBroadcastService.broadcast(response);

        // 打印响应结果
        log.info("=== 异步通知回调接口响应结果 ===");
        log.info(JSONObject.toJSONString(response, true));

        return response;
    }

    private String buildNotifySignSource(Map<String, Object> request, MerchantEnum merchantEnum, String signContent) {
        StringBuilder sb = new StringBuilder();
        sb.append("method=").append(stringValue(request.get("method")));
        sb.append("&version=").append(stringValue(request.get("version")));
        sb.append("&format=").append(stringValue(request.get("format")));
        sb.append("&merchantNo=").append(merchantEnum.getMerchantNo());
        sb.append("&signType=").append(merchantEnum.getSignType());
        sb.append("&signContent=").append(signContent);
        sb.append("&key=").append(merchantEnum.getPwd());
        return sb.toString();
    }

    private Map<String, Object> parseSignContent(String signContent) {
        return JSONObject.parseObject(signContent, new com.alibaba.fastjson.TypeReference<Map<String, Object>>() {});
    }

    private String getRequiredString(Map<String, Object> request, String key) {
        String value = stringValue(request.get(key));
        if (value == null || value.isEmpty()) {
            throw new RuntimeException("缺少参数: " + key);
        }
        return value;
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}

package com.payment.gateway.utils;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import com.payment.gateway.enums.MerchantEnum;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpClient {

    public JSONObject post(Map<String, String> map, String url, MerchantEnum merchantEnum) throws Exception {

        ClassicHttpRequest postReq = ClassicRequestBuilder
                .post(url)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept-Encoding", "gzip,deflate")
                .addHeader("Connection", "Keep-Alive")
                .addHeader("Accept-Language", "zh-CN")
                .build();
        List<NameValuePair> nameValuePairList = new ArrayList<>();
        if("RSA2".equals(merchantEnum.getSignType())){
            if(map.get("merchantNo") != null){
                // 加签
                map.put("sign", sign(map, merchantEnum));
            }
        } else if ("CFCA".equals(merchantEnum.getSignType())){
            // 加密
            map.put("signContent", RSAHelper.encrypt(map.get("signContent"),
                    getClass().getClassLoader().getResource(merchantEnum.getPrivateKey()).getPath(),
                    merchantEnum.getPriKeyPass()));
        }

        System.out.println("请求地址：" + url);
        System.out.println("请求参数：" + new Gson().toJson(map));
        if (!map.isEmpty()) {
            //获取map所有的key，并放入set集合中
            Set<String> keySet = map.keySet();
            for (String key : keySet) {
                nameValuePairList.add(new BasicNameValuePair(key, map.get(key)));
            }
        }
        postReq.setEntity(new UrlEncodedFormEntity(nameValuePairList, Charset.forName("UTF-8")));

        JSONObject respJSON = null;

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            respJSON = client.execute(
                    postReq,
                    classicHttpResponse -> {
                        String response = "";
                        if (classicHttpResponse.getCode() == 200) {
                            response = EntityUtils.toString(classicHttpResponse.getEntity());
                            System.out.println("返回报文：" + response);
                        } else {
                            System.out.println("服务端响应失败 ！！！");
                        }
                        return JSONObject.parseObject(response);
                    });
        }

        if ("RSA2".equals(merchantEnum.getSignType()) && respJSON.getString("success").equals("true")) {
            // 验签
            verify(respJSON, merchantEnum);
        } else if("CFCA".equals(merchantEnum.getSignType()) && respJSON.getString("success").equals("true")) {
            // 解密
            respJSON.put("result", new String(
                    Base64.getDecoder().decode(
                        RSAHelper.decrypt(respJSON.getString("result"),
                        getClass().getClassLoader().getResource(merchantEnum.getPublicKey()).getPath())
                    ), StandardCharsets.UTF_8));

            System.out.println("解密后的报文：" + respJSON.getString("result"));
        }
        return JSONObject.parseObject(respJSON.getString("result"));
    }

    private String sign(Map<String, String> map, MerchantEnum merchantEnum) {

        StringBuilder sb = new StringBuilder();

        sb.append("method=" + map.get("method"));
        sb.append("&version=" + map.get("version"));
        sb.append("&format=JSON");
        sb.append("&merchantNo=" + merchantEnum.getMerchantNo());
        sb.append("&signType=" + merchantEnum.getSignType());
        sb.append("&signContent=" + map.get("signContent"));
        sb.append("&key=" + merchantEnum.getPwd());

        return RSA2Helper.sign(sb.toString(),
                getClass().getClassLoader().getResource(merchantEnum.getPrivateKey()).getPath(),
                merchantEnum.getPriKeyPass());
    }

    private void verify(JSONObject respJson, MerchantEnum merchantEnum) throws Exception {
        //验签，接收响应报文，传入待验签明文，
        StringBuilder ss = new StringBuilder();
        boolean success = respJson.getBoolean("success");
        if (success) {
            ss.append("result=" + respJson.getString("result"));
        } else {
            ss.append("errorCode=" + respJson.getString("errorCode"));
            ss.append("&errorMsg=" + respJson.getString("errorMsg"));
        }
        ss.append("&success=" + respJson.getString("success"));
        ss.append("&key=" + merchantEnum.getPwd());

        boolean result = RSA2Helper.verify(ss.toString(), respJson.getString("sign"),
                getClass().getClassLoader().getResource(merchantEnum.getPublicKey()).getPath());
        System.out.println("验签结果：" + result);

        if (!result) {
            throw new RuntimeException("验签失败");
        }
    }
}

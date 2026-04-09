package com.payment.gateway.controller;

import com.payment.gateway.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/merchant")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    @GetMapping("/lists")
    public List<Map<String, Object>> getMerchantList() {
        return merchantService.getMerchantList();
    }

    @GetMapping("/environments")
    public List<Map<String, String>> getEnvironmentList() {
        // 返回环境列表：dev, pre, pro
        List<Map<String, String>> environments = new ArrayList<>();
        
        Map<String, String> dev = new HashMap<>();
        dev.put("code", "dev");
        dev.put("name", "开发环境");
        dev.put("url","https://test-api.huishouqian.com");
        environments.add(dev);
        
        Map<String, String> pre = new HashMap<>();
        pre.put("code", "pre");
        pre.put("name", "预生产环境");
        pre.put("url","https://api-pre.huishouqian.com");

        environments.add(pre);
        
        Map<String, String> pro = new HashMap<>();
        pro.put("code", "pro");
        pro.put("name", "生产环境");
        pro.put("url", "https://api.huishouqian.com");
        environments.add(pro);
        
        return environments;
    }

}
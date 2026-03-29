package com.payment.gateway.service;

import com.payment.gateway.entity.Merchant;
import com.payment.gateway.repository.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MerchantService {

    @Autowired
    private MerchantRepository merchantRepository;

    public List<Map<String, Object>> getMerchantList() {
        // 获取所有启用的商户
        List<Merchant> merchants = merchantRepository.findByStatus("1");
        // 转换为包含商户号、环境、商户名、签名类型和回调URL的Map列表
        return merchants.stream()
                .map(merchant -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("merchantNo", merchant.getMerchantNo());
                    map.put("environment", merchant.getEnvironment());
                    map.put("merchantName", merchant.getMerchantName());
                    map.put("signType", merchant.getSignType());
                    map.put("url", merchant.getUrl());
                    return map;
                })
                .collect(Collectors.toList());
    }
}
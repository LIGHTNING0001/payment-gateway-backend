package com.payment.gateway.service;

import com.payment.gateway.entity.PaymentMethod;
import com.payment.gateway.repository.PaymentMethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentMethodService {

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    public List<PaymentMethod> getPaymentMethods() {
        // 获取所有启用的支付方式
        return paymentMethodRepository.findByStatus("1");
    }

}
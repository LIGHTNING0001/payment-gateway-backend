package com.payment.gateway.controller;

import com.payment.gateway.service.PaymentMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/pay")
public class PaymentMethodController {

    @Autowired
    private PaymentMethodService paymentMethodService;

    @GetMapping("/methods")
    public List<com.payment.gateway.entity.PaymentMethod> getPaymentMethods() {
        return paymentMethodService.getPaymentMethods();
    }

}
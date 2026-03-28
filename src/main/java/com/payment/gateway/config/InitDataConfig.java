package com.payment.gateway.config;

import com.payment.gateway.entity.Merchant;
import com.payment.gateway.entity.PaymentMethod;
import com.payment.gateway.enums.MerchantEnum;
import com.payment.gateway.repository.MerchantRepository;
import com.payment.gateway.repository.PaymentMethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class InitDataConfig implements CommandLineRunner {

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Override
    public void run(String... args) throws Exception {
        // 初始化商户数据
        initMerchants();

        // 初始化支付方式数据
        initPaymentMethods();
    }

    private void initMerchants() {
        // 检查是否已有商户数据
        if (merchantRepository.count() == 0) {
            // 从MerchantEnum初始化所有商户
            for (MerchantEnum merchantEnum : MerchantEnum.values()) {
                Merchant merchant = new Merchant();
                merchant.setMerchantNo(merchantEnum.getMerchantNo());
                merchant.setMerchantName(merchantEnum.getMerchantName());
                merchant.setSignType(merchantEnum.getSignType());
                merchant.setPrivateKeyPath(merchantEnum.getPrivateKey());
                merchant.setPublicKeyPath(merchantEnum.getPublicKey());
                merchant.setPrivateKeyPwd(merchantEnum.getPriKeyPass());
                merchant.setStatus("1");
                // 根据枚举名称判断环境
                String environment = merchantEnum.getUrl().equals("https://api-pre.huishouqian.com") ? "pro" : "dev";
                merchant.setEnvironment(environment);
                merchantRepository.save(merchant);
            }
        }
    }

    private void initPaymentMethods() {
        // 检查是否已有支付方式数据
        if (paymentMethodRepository.count() == 0) {
            // 创建支付方式
            PaymentMethod alipay = new PaymentMethod();
            alipay.setMethodCode("ALI_NATIVE");
            alipay.setMethodName("支付宝扫码支付");
            alipay.setChannel("alipay");
            alipay.setRate(new BigDecimal("0.0038"));
            alipay.setStatus("1");
            paymentMethodRepository.save(alipay);

            PaymentMethod wechat = new PaymentMethod();
            wechat.setMethodCode("WECHAT_NATIVE");
            wechat.setMethodName("微信扫码支付");
            wechat.setChannel("wechat");
            wechat.setRate(new BigDecimal("0.0038"));
            wechat.setStatus("1");
            paymentMethodRepository.save(wechat);

            PaymentMethod alipayH5 = new PaymentMethod();
            alipayH5.setMethodCode("ALI_H5");
            alipayH5.setMethodName("支付宝H5支付");
            alipayH5.setChannel("alipay");
            alipayH5.setRate(new BigDecimal("0.0038"));
            alipayH5.setStatus("1");
            paymentMethodRepository.save(alipayH5);

            PaymentMethod wechatH5 = new PaymentMethod();
            wechatH5.setMethodCode("WECHAT_H5");
            wechatH5.setMethodName("微信H5支付");
            wechatH5.setChannel("wechat");
            wechatH5.setRate(new BigDecimal("0.0038"));
            wechatH5.setStatus("1");
            paymentMethodRepository.save(wechatH5);
        }
    }

}
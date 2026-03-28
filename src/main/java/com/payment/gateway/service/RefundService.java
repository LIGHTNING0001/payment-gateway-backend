package com.payment.gateway.service;

import com.payment.gateway.entity.Refund;
import com.payment.gateway.entity.Order;
import com.payment.gateway.repository.RefundRepository;
import com.payment.gateway.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.math.BigDecimal;

@Service
public class RefundService {

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private WechatService wechatService;

    public Refund createRefund(Map<String, Object> signContent, String merchantNo) {
        // 生成退款单号
        String transNo = "REFUND" + new SimpleDateFormat("yyyyMMddHHmmssss").format(new Date());
        signContent.put("transNo", transNo);

        // 验证原订单是否存在
        String origTransNo = signContent.get("origTransNo").toString();
        Order order = orderRepository.findByTransNo(origTransNo);
        if (order == null) {
            throw new RuntimeException("原订单不存在");
        }

        // 创建退款实体
        Refund refund = new Refund();
        refund.setTransNo(transNo);
        refund.setOrigTransNo(origTransNo);
        refund.setMerchantNo(merchantNo);
        refund.setRefundType(signContent.get("refundType").toString());
        refund.setOrigOrderAmt(new BigDecimal(signContent.get("origOrderAmt").toString()));
        refund.setOrderAmt(new BigDecimal(signContent.get("orderAmt").toString()));
        refund.setRequestDate(signContent.get("requestDate").toString());
        refund.setRefundReason(signContent.get("refundReason").toString());
        refund.setReturnUrl(signContent.get("returnUrl").toString());
        refund.setStatus("0"); // 待处理

        // 保存退款记录
        refund = refundRepository.save(refund);

        // 根据原订单的支付类型调用不同的退款接口
        String payType = order.getPayType();
        String tradeNo = "";
        String status = "";

        if (payType.contains("ALI")) {
            // 调用支付宝退款接口
            Map<String, String> result = alipayService.refund(order, refund);
            tradeNo = result.get("tradeNo");
            status = result.get("status");
        } else if (payType.contains("WECHAT")) {
            // 调用微信退款接口
            Map<String, String> result = wechatService.refund(order, refund);
            tradeNo = result.get("tradeNo");
            status = result.get("status");
        }

        refund.setTradeNo(tradeNo);
        refund.setStatus(status);
        refundRepository.save(refund);

        // 更新原订单状态
        if (status.equals("1")) {
            order.setStatus("3"); // 已退款
            orderRepository.save(order);
        }

        return refund;
    }

    public Refund queryRefund(String transNo) {
        return refundRepository.findByTransNo(transNo);
    }

}
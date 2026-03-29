package com.payment.gateway.service;

import com.payment.gateway.entity.Order;
import com.payment.gateway.repository.OrderRepository;
import com.payment.gateway.utils.RSA2Helper;
import com.payment.gateway.utils.CFCAHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Service
public class PaymentService {

    @Autowired
    private OrderRepository orderRepository;

    public Order createOrder(Map<String, Object> signContent, String merchantNo) {
        // 生成订单号
        String transNo = new SimpleDateFormat("yyyyMMddHHmmssss").format(new Date());
        signContent.put("transNo", transNo);

        // 创建订单实体
        Order order = new Order();
        order.setTransNo(transNo);
        order.setMerchantNo(merchantNo);
        order.setOrderAmt(new BigDecimal(signContent.get("orderAmt").toString()));
        order.setTotalOrderAmt(new BigDecimal(signContent.get("totalOrderAmt").toString()));
        order.setPayType(signContent.get("payType").toString());
        order.setGoodsInfo(signContent.get("goodsInfo").toString());
        order.setRequestDate(signContent.get("requestDate").toString());
        order.setReturnUrl(signContent.get("returnUrl").toString());
        order.setStatus("0"); // 待支付
        order.setMemo(signContent.get("memo").toString());

        // 保存订单
        order = orderRepository.save(order);

        return order;
    }

    public void updateOrder(Order order) {
        orderRepository.save(order);
    }

}
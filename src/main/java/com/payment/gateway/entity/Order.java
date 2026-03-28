package com.payment.gateway.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "payment_order")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trans_no", unique = true, nullable = false)
    private String transNo;

    @Column(name = "merchant_no", nullable = false)
    private String merchantNo;

    @Column(name = "order_amt", nullable = false)
    private BigDecimal orderAmt;

    @Column(name = "total_order_amt", nullable = false)
    private BigDecimal totalOrderAmt;

    @Column(name = "pay_type")
    private String payType;

    @Column(name = "goods_info")
    private String goodsInfo;

    @Column(name = "request_date")
    private String requestDate;

    @Column(name = "return_url")
    private String returnUrl;

    @Column(name = "notify_url")
    private String notifyUrl;

    @Column(name = "qr_code", columnDefinition = "text")
    private String qrCode;

    @Column(name = "trade_no")
    private String tradeNo;

    @Column(name = "status")
    private String status; // 0: 待支付, 1: 支付成功, 2: 支付失败, 3: 已退款

    @Column(name = "memo", columnDefinition = "text")
    private String memo;

    @CreationTimestamp
    @Column(name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

}

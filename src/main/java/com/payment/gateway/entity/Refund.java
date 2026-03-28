package com.payment.gateway.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "payment_refund")
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trans_no", unique = true, nullable = false)
    private String transNo;

    @Column(name = "orig_trans_no", nullable = false)
    private String origTransNo;

    @Column(name = "merchant_no", nullable = false)
    private String merchantNo;

    @Column(name = "refund_type")
    private String refundType;

    @Column(name = "orig_order_amt", nullable = false)
    private BigDecimal origOrderAmt;

    @Column(name = "order_amt", nullable = false)
    private BigDecimal orderAmt;

    @Column(name = "request_date")
    private String requestDate;

    @Column(name = "refund_reason")
    private String refundReason;

    @Column(name = "return_url")
    private String returnUrl;

    @Column(name = "trade_no")
    private String tradeNo;

    @Column(name = "status")
    private String status; // 0: 待处理, 1: 退款成功, 2: 退款失败

    @CreationTimestamp
    @Column(name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

}
package com.payment.gateway.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "payment_method")
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "method_code", unique = true, nullable = false)
    private String methodCode;

    @Column(name = "method_name", nullable = false)
    private String methodName;

    @Column(name = "channel")
    private String channel; // alipay, wechat, baofu, etc.

    @Column(name = "rate")
    private BigDecimal rate; // 费率

    @Column(name = "status")
    private String status; // 0: 禁用, 1: 启用

    @CreationTimestamp
    @Column(name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

}
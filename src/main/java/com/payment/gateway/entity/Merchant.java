package com.payment.gateway.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "merchant")
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_no", unique = true, nullable = false)
    private String merchantNo;

    @Column(name = "merchant_name", nullable = false)
    private String merchantName;

    @Column(name = "sign_type")
    private String signType; // RSA2, CFCA

    @Column(name = "private_key_path")
    private String privateKeyPath;

    @Column(name = "public_key_path")
    private String publicKeyPath;

    @Column(name = "private_key_pwd")
    private String privateKeyPwd;

    @Column(name = "status")
    private String status; // 0: 禁用, 1: 启用

    @Column(name = "environment")
    private String environment; // dev, pre, pro

    @Column(name = "url")
    private String url; // 商户回调URL

    @CreationTimestamp
    @Column(name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

}
package com.payment.gateway.repository;

import com.payment.gateway.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    Refund findByTransNo(String transNo);

    Refund findByOrigTransNo(String origTransNo);

}
package com.payment.gateway.repository;

import com.payment.gateway.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    Merchant findByMerchantNo(String merchantNo);

    List<Merchant> findByStatus(String status);

}
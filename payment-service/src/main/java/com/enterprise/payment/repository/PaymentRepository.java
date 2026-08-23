package com.enterprise.payment.repository;

import com.enterprise.payment.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByTransactionId(String transactionId);

    List<PaymentEntity> findByOrderIdOrderByCreatedAtDesc(String orderId);

    Optional<PaymentEntity> findByOrderIdAndStatus(String orderId, PaymentEntity.PaymentStatus status);
}

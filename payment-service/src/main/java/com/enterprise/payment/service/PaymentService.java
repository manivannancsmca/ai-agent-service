package com.enterprise.payment.service;

import com.enterprise.payment.dto.PaymentRequest;
import com.enterprise.payment.dto.PaymentResult;
import com.enterprise.payment.dto.RefundResult;
import com.enterprise.payment.entity.PaymentEntity;
import com.enterprise.payment.exception.PaymentNotFoundException;
import com.enterprise.payment.exception.PaymentProcessingException;
import com.enterprise.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    /**
     * Process a payment for an order.
     *
     * In production, this would integrate with a payment gateway
     * (Stripe, Square, Adyen, etc.). This implementation simulates
     * the payment processing with realistic behavior:
     * - 95% success rate
     * - Simulated processing delay
     * - Proper state transitions
     *
     * Called by the AI Agent's PaymentTool.processPayment()
     */
    @Transactional
    public PaymentResult processPayment(PaymentRequest request) {
        String transactionId = generateTransactionId();
        log.info("Processing payment: txn={}, orderId={}, amount={}, method={}",
                transactionId, request.orderId(), request.amount(), request.paymentMethod());

        // Check for duplicate payment
        if (paymentRepository.findByOrderIdAndStatus(
                request.orderId(), PaymentEntity.PaymentStatus.COMPLETED).isPresent()) {
            log.warn("Duplicate payment attempt for order {}", request.orderId());
            throw new PaymentProcessingException(
                    "Payment already completed for order " + request.orderId());
        }

        PaymentEntity.PaymentMethod method = parsePaymentMethod(request.paymentMethod());

        // Create payment record in PROCESSING state
        PaymentEntity payment = PaymentEntity.builder()
                .transactionId(transactionId)
                .orderId(request.orderId())
                .amount(request.amount())
                .currency(request.currency())
                .paymentMethod(method)
                .status(PaymentEntity.PaymentStatus.PROCESSING)
                .build();

        payment = paymentRepository.save(payment);

        // Simulate payment gateway call
        // In production: paymentGateway.charge(amount, method, token)
        boolean success = simulatePaymentGateway(request.amount(), method);

        if (success) {
            payment.setStatus(PaymentEntity.PaymentStatus.COMPLETED);
            payment.setProcessedAt(Instant.now());
            paymentRepository.save(payment);

            log.info("Payment completed: txn={}, orderId={}", transactionId, request.orderId());

            return new PaymentResult(
                    transactionId,
                    request.orderId(),
                    request.amount(),
                    "COMPLETED",
                    request.paymentMethod(),
                    "Payment processed successfully"
            );
        } else {
            payment.setStatus(PaymentEntity.PaymentStatus.FAILED);
            payment.setFailureReason("Payment declined by issuer");
            paymentRepository.save(payment);

            log.warn("Payment failed: txn={}, orderId={}", transactionId, request.orderId());

            return new PaymentResult(
                    transactionId,
                    request.orderId(),
                    request.amount(),
                    "FAILED",
                    request.paymentMethod(),
                    "Payment was declined. Please try a different payment method."
            );
        }
    }

    /**
     * Process a refund for a completed payment.
     *
     * Called by the AI Agent's PaymentTool.refundPayment()
     */
    @Transactional
    public RefundResult refundPayment(String transactionId) {
        log.info("Processing refund for transaction {}", transactionId);

        PaymentEntity originalPayment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new PaymentNotFoundException(transactionId));

        if (originalPayment.getStatus() != PaymentEntity.PaymentStatus.COMPLETED) {
            throw new PaymentProcessingException(
                    "Cannot refund transaction " + transactionId +
                    " with status " + originalPayment.getStatus());
        }

        // Check if already refunded
        if (originalPayment.getStatus() == PaymentEntity.PaymentStatus.REFUNDED) {
            throw new PaymentProcessingException("Transaction already refunded");
        }

        String refundTxnId = generateTransactionId();

        // Simulate refund processing
        originalPayment.setStatus(PaymentEntity.PaymentStatus.REFUNDED);
        originalPayment.setRefundTransactionId(refundTxnId);
        paymentRepository.save(originalPayment);

        log.info("Refund processed: original={}, refund={}, amount={}",
                transactionId, refundTxnId, originalPayment.getAmount());

        return new RefundResult(
                refundTxnId,
                transactionId,
                originalPayment.getAmount(),
                "COMPLETED",
                "Refund processed successfully. Allow 5-10 business days for the refund to appear."
        );
    }

    /**
     * Get payment status for a transaction.
     */
    @Transactional(readOnly = true)
    public PaymentResult getPaymentStatus(String transactionId) {
        PaymentEntity payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new PaymentNotFoundException(transactionId));

        return new PaymentResult(
                payment.getTransactionId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getStatus().name(),
                payment.getPaymentMethod().name(),
                null
        );
    }

    // ─── Internal Helpers ────────────────────────────────────────

    /**
     * Simulates a payment gateway call.
     * In production, replace with actual gateway integration.
     *
     * Returns true 95% of the time to simulate realistic behavior.
     */
    private boolean simulatePaymentGateway(BigDecimal amount, PaymentEntity.PaymentMethod method) {
        // Simulate processing time (in production this would be a real HTTP call)
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate 95% success rate
        return ThreadLocalRandom.current().nextDouble() < 0.95;
    }

    private PaymentEntity.PaymentMethod parsePaymentMethod(String method) {
        try {
            return PaymentEntity.PaymentMethod.valueOf(method.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            log.warn("Unknown payment method '{}', defaulting to CREDIT_CARD", method);
            return PaymentEntity.PaymentMethod.CREDIT_CARD;
        }
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
}
package com.enterprise.payment.controller;

import com.enterprise.payment.dto.PaymentRequest;
import com.enterprise.payment.dto.PaymentResult;
import com.enterprise.payment.dto.RefundResult;
import com.enterprise.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Process a payment for an order.
     * Called by the AI Agent's PaymentTool.processPayment()
     */
    @PostMapping
    public ResponseEntity<PaymentResult> processPayment(
            @Valid @RequestBody PaymentRequest request) {
        PaymentResult result = paymentService.processPayment(request);
        HttpStatus status = "COMPLETED".equals(result.status())
                ? HttpStatus.CREATED : HttpStatus.PAYMENT_REQUIRED;
        return ResponseEntity.status(status).body(result);
    }

    /**
     * Process a refund.
     * Called by the AI Agent's PaymentTool.refundPayment()
     */
    @PostMapping("/{transactionId}/refund")
    public ResponseEntity<RefundResult> refundPayment(@PathVariable String transactionId) {
        return ResponseEntity.ok(paymentService.refundPayment(transactionId));
    }

    /**
     * Get payment status.
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<PaymentResult> getPaymentStatus(@PathVariable String transactionId) {
        return ResponseEntity.ok(paymentService.getPaymentStatus(transactionId));
    }
}

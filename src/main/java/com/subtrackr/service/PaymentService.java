package com.subtrackr.service;

import com.subtrackr.model.Payment;
import com.subtrackr.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public List<Payment> getPaymentsByUser(int userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public Payment addPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment createPayment(int userId, String orderId, double amount, String plan) {
        Payment payment = new Payment();
        payment.setUserId(userId);
        payment.setOrderId(orderId);
        // Converting paisa to rupees if passed as paisa
        payment.setAmount(amount / 100.0);
        payment.setPlan(plan);
        payment.setStatus("created");
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment updatePaymentStatus(String orderId, String paymentId, String status) {
        Optional<Payment> optPayment = paymentRepository.findByOrderId(orderId);
        if (optPayment.isPresent()) {
            Payment payment = optPayment.get();
            payment.setPaymentId(paymentId);
            payment.setStatus(status);
            return paymentRepository.save(payment);
        }
        return null;
    }

    public double getTotalRevenue() {
        return paymentRepository.getTotalRevenue();
    }
}

package com.subtrackr.repository;

import com.subtrackr.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    List<Payment> findByUserIdOrderByCreatedAtDesc(int userId);

    Optional<Payment> findByOrderId(String orderId);

    @Query("SELECT COALESCE(SUM(p.amount), 0.0) FROM Payment p WHERE LOWER(p.status) IN ('success', 'paid')")
    double getTotalRevenue();

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("DELETE FROM Payment p WHERE p.userId = :userId")
    void deleteByUserId(@org.springframework.data.repository.query.Param("userId") int userId);
}

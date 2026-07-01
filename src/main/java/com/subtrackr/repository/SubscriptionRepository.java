package com.subtrackr.repository;

import com.subtrackr.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {

    List<Subscription> findByUserIdAndStatusNotOrderByRenewalDateAsc(int userId, String status);

    int countByUserIdAndStatus(int userId, String status);

    @Query("SELECT s FROM Subscription s WHERE s.status = 'active' AND s.renewalDate >= CURRENT_DATE AND s.renewalDate <= :maxDate")
    List<Subscription> findUpcomingRenewals(@Param("maxDate") LocalDate maxDate);

    @Query("SELECT s, u.email, u.plan, u.fullName FROM Subscription s JOIN User u ON s.userId = u.id " +
           "WHERE s.status = 'active' AND s.renewalDate >= CURRENT_DATE AND s.renewalDate <= :maxDate")
    List<Object[]> findUpcomingRenewalsWithUserDetails(@Param("maxDate") LocalDate maxDate);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("DELETE FROM Subscription s WHERE s.userId = :userId")
    void deleteByUserId(@Param("userId") int userId);
}

package com.subtrackr.repository;

import com.subtrackr.model.ReminderSent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReminderSentRepository extends JpaRepository<ReminderSent, Integer> {
    boolean existsBySubscriptionIdAndReminderType(int subscriptionId, String reminderType);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("DELETE FROM ReminderSent r WHERE r.subscriptionId = :subscriptionId")
    void deleteBySubscriptionId(@org.springframework.data.repository.query.Param("subscriptionId") int subscriptionId);
}

package com.subtrackr.service;

import com.subtrackr.model.Subscription;
import com.subtrackr.model.User;
import com.subtrackr.repository.SubscriptionRepository;
import com.subtrackr.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Autowired
    public SubscriptionService(SubscriptionRepository subscriptionRepository, UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
    }

    public List<Subscription> getSubscriptionsByUser(int userId) {
        return subscriptionRepository.findByUserIdAndStatusNotOrderByRenewalDateAsc(userId, "cancelled");
    }

    public int countActiveSubscriptions(int userId) {
        return subscriptionRepository.countByUserIdAndStatus(userId, "active");
    }

    @Transactional
    public Subscription addSubscription(Subscription subscription) {
        // Find user to check plan limit
        Optional<User> optUser = userRepository.findById(subscription.getUserId());
        if (optUser.isPresent()) {
            User user = optUser.get();
            if ("basic".equals(user.getPlan()) && countActiveSubscriptions(user.getId()) >= 4) {
                throw new IllegalStateException("limit_reached");
            }
        }
        if (subscription.getStatus() == null) {
            subscription.setStatus("active");
        }
        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public boolean deleteSubscription(int id) {
        if (subscriptionRepository.existsById(id)) {
            subscriptionRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean updateStatus(int id, String status) {
        Optional<Subscription> optSub = subscriptionRepository.findById(id);
        if (optSub.isPresent()) {
            Subscription sub = optSub.get();
            sub.setStatus(status);
            subscriptionRepository.save(sub);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean updateRenewalDate(int id, LocalDate newDate) {
        Optional<Subscription> optSub = subscriptionRepository.findById(id);
        if (optSub.isPresent()) {
            Subscription sub = optSub.get();
            sub.setRenewalDate(newDate);
            subscriptionRepository.save(sub);
            return true;
        }
        return false;
    }

    public List<Subscription> getUpcomingRenewals(int daysAhead) {
        LocalDate maxDate = LocalDate.now().plusDays(daysAhead);
        List<Object[]> results = subscriptionRepository.findUpcomingRenewalsWithUserDetails(maxDate);
        List<Subscription> list = new ArrayList<>();
        for (Object[] row : results) {
            Subscription s = (Subscription) row[0];
            s.setUserEmail((String) row[1]);
            s.setUserPlan((String) row[2]);
            s.setUserName((String) row[3]);
            list.add(s);
        }
        return list;
    }
}

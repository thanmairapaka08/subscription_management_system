package com.subtrackr.service;

import com.subtrackr.model.ReminderSent;
import com.subtrackr.model.Subscription;
import com.subtrackr.repository.ReminderSentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReminderSchedulerService {

    private final SubscriptionService subscriptionService;
    private final ReminderSentRepository reminderSentRepository;
    private final EmailService emailService;

    @Autowired
    public ReminderSchedulerService(SubscriptionService subscriptionService,
                                   ReminderSentRepository reminderSentRepository,
                                   EmailService emailService) {
        this.subscriptionService = subscriptionService;
        this.reminderSentRepository = reminderSentRepository;
        this.emailService = emailService;
    }

    // Run every day at 9 AM
    @Scheduled(cron = "0 0 9 * * ?")
    public void runDailyReminderCheck() {
        checkAndSendReminders();
    }

    // Run on startup (after 10 seconds delay) and every 24 hours
    @Scheduled(initialDelay = 10000, fixedRate = 86400000)
    @Transactional
    public void checkAndSendReminders() {
        System.out.println("Running reminder scheduler check...");
        try {
            LocalDate today = LocalDate.now();
            // Fetch subscriptions with upcoming renewals in 7 days
            List<Subscription> upcoming = subscriptionService.getUpcomingRenewals(7);

            for (Subscription sub : upcoming) {
                long daysUntilRenewal = ChronoUnit.DAYS.between(today, sub.getRenewalDate());
                
                if (daysUntilRenewal == 7) {
                    processReminder(sub, "7_days", 7);
                } else if ("premium".equalsIgnoreCase(sub.getUserPlan()) && daysUntilRenewal == 5) {
                    processReminder(sub, "5_days", 5);
                } else if (daysUntilRenewal == 3) {
                    processReminder(sub, "3_days", 3);
                } else if ("premium".equalsIgnoreCase(sub.getUserPlan()) && daysUntilRenewal == 1) {
                    processReminder(sub, "1_day", 1);
                }
            }
        } catch (Exception e) {
            System.err.println("Error in ReminderSchedulerService execution:");
            e.printStackTrace();
        }
    }

    private void processReminder(Subscription sub, String reminderType, int daysLeft) {
        boolean sent = reminderSentRepository.existsBySubscriptionIdAndReminderType(sub.getId(), reminderType);
        if (!sent) {
            if (sub.getUserEmail() != null) {
                emailService.sendReminderEmail(sub.getUserEmail(), sub.getUserName(), sub.getName(), sub.getRenewalDate(), daysLeft);
                ReminderSent record = new ReminderSent(sub.getId(), reminderType);
                reminderSentRepository.save(record);
            }
        }
    }
}

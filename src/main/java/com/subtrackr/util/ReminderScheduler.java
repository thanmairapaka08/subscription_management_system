package com.subtrackr.util;

import com.subtrackr.dao.SubscriptionDAO;
import com.subtrackr.dao.UserDAO;
import com.subtrackr.model.Subscription;
import com.subtrackr.model.User;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebListener
public class ReminderScheduler implements ServletContextListener {

    private ScheduledExecutorService scheduler;
    private SubscriptionDAO subscriptionDAO = new SubscriptionDAO();
    private UserDAO userDAO = new UserDAO();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        // Run every 24 hours, start immediately
        scheduler.scheduleAtFixedRate(this::checkAndSendReminders, 0, 24, TimeUnit.HOURS);
        System.out.println("ReminderScheduler initialized.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        System.out.println("ReminderScheduler destroyed.");
    }

    private void checkAndSendReminders() {
        System.out.println("Running daily reminder check...");
        try {
            LocalDate today = LocalDate.now();
            List<Subscription> upcoming = subscriptionDAO.getUpcomingRenewals(7);

            for (Subscription sub : upcoming) {
                long daysUntilRenewal = ChronoUnit.DAYS.between(today, sub.getRenewalDate());
                
                if (daysUntilRenewal == 7) {
                    processReminder(sub, "7_days", 7);
                } else if (daysUntilRenewal == 3) {
                    processReminder(sub, "3_days", 3);
                }
            }
        } catch (Exception e) {
            System.err.println("Error in ReminderScheduler:");
            e.printStackTrace();
        }
    }

    private void processReminder(Subscription sub, String reminderType, int daysLeft) {
        if (!isReminderSent(sub.getId(), reminderType)) {
            User user = userDAO.getUserById(sub.getUserId());
            if (user != null && user.getEmail() != null) {
                EmailUtil.sendReminderEmail(user.getEmail(), user.getFullName(), sub.getName(), sub.getRenewalDate(), daysLeft);
                recordReminderSent(sub.getId(), reminderType);
            }
        }
    }

    private boolean isReminderSent(int subscriptionId, String reminderType) {
        String sql = "SELECT id FROM reminders_sent WHERE subscription_id = ? AND reminder_type = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, subscriptionId);
            stmt.setString(2, reminderType);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void recordReminderSent(int subscriptionId, String reminderType) {
        String sql = "INSERT INTO reminders_sent (subscription_id, reminder_type) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, subscriptionId);
            stmt.setString(2, reminderType);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

package com.subtrackr.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendReminderEmail(String toEmail, String userName, String subscriptionName, LocalDate renewalDate, int daysLeft) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("hello.subtrackr@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("⏰ SubTrackr: " + subscriptionName + " renews in " + daysLeft + " days");

            String htmlBody = "<div style='font-family: Arial, sans-serif; background-color: #0F172A; color: #F8FAFC; padding: 20px; border-radius: 10px;'>"
                    + "<h2 style='color: #6366F1;'>Subscription Reminder</h2>"
                    + "<p>Hi " + userName + ",</p>"
                    + "<p>Your <strong>" + subscriptionName + "</strong> subscription is set to renew on <strong>" + renewalDate + "</strong>.</p>"
                    + "<p>If you wish to cancel, please do it before the renewal date.</p>"
                    + "<br>"
                    + "<a href='http://localhost:8085/dashboard' style='background-color: #6366F1; color: #FFF; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>View Dashboard</a>"
                    + "</div>";

            helper.setText(htmlBody, true);
            mailSender.send(message);
            System.out.println("Reminder email sent successfully to " + toEmail + " for " + subscriptionName);
        } catch (MessagingException e) {
            System.err.println("Failed to send email to: " + toEmail);
            e.printStackTrace();
        }
    }
}

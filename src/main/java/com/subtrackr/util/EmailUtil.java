package com.subtrackr.util;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.util.Properties;

// Maven dependency required:
// <dependency>
//     <groupId>com.sun.mail</groupId>
//     <artifactId>jakarta.mail</artifactId>
//     <version>2.0.1</version>
// </dependency>

public class EmailUtil {

    private static final String EMAIL_FROM = "your-email@gmail.com"; // PLACEHOLDER
    private static final String EMAIL_PASSWORD = "your-app-password"; // PLACEHOLDER

    public static void sendReminderEmail(String toEmail, String userName, String subscriptionName, LocalDate renewalDate, int daysLeft) {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); // TLS

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("⏰ SubTrackr: " + subscriptionName + " renews in " + daysLeft + " days");

            String htmlBody = "<div style='font-family: Arial, sans-serif; background-color: #0F172A; color: #F8FAFC; padding: 20px; border-radius: 10px;'>"
                    + "<h2 style='color: #6366F1;'>Subscription Reminder</h2>"
                    + "<p>Hi " + userName + ",</p>"
                    + "<p>Your <strong>" + subscriptionName + "</strong> subscription is set to renew on <strong>" + renewalDate + "</strong>.</p>"
                    + "<p>If you wish to cancel, please do it before the renewal date.</p>"
                    + "<br>"
                    + "<a href='http://localhost:8080/SubTrackr/dashboard' style='background-color: #6366F1; color: #FFF; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>View Dashboard</a>"
                    + "</div>";

            message.setContent(htmlBody, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("Reminder email sent successfully to " + toEmail + " for " + subscriptionName);

        } catch (MessagingException e) {
            System.err.println("Failed to send email... ");
            e.printStackTrace();
        }
    }
}

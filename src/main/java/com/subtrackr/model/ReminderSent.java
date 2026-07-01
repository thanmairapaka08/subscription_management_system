package com.subtrackr.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "reminders_sent")
public class ReminderSent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "subscription_id", nullable = false)
    private int subscriptionId;

    @Column(name = "reminder_type", nullable = false)
    private String reminderType;

    @Column(name = "sent_at", insertable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp sentAt;

    public ReminderSent() {}

    public ReminderSent(int subscriptionId, String reminderType) {
        this.subscriptionId = subscriptionId;
        this.reminderType = reminderType;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(int subscriptionId) { this.subscriptionId = subscriptionId; }

    public String getReminderType() { return reminderType; }
    public void setReminderType(String reminderType) { this.reminderType = reminderType; }

    public Timestamp getSentAt() { return sentAt; }
    public void setSentAt(Timestamp sentAt) { this.sentAt = sentAt; }
}

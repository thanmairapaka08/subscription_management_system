package com.subtrackr.dao;

import com.subtrackr.model.Subscription;
import com.subtrackr.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionDAO {
    
    public boolean addSubscription(Subscription s) {
        String sql = "INSERT INTO subscriptions (user_id, name, category, amount, billing_cycle, renewal_date, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, s.getUserId());
            stmt.setString(2, s.getName());
            stmt.setString(3, s.getCategory());
            stmt.setDouble(4, s.getAmount());
            stmt.setString(5, s.getBillingCycle());
            stmt.setDate(6, Date.valueOf(s.getRenewalDate()));
            stmt.setString(7, s.getStatus() != null ? s.getStatus() : "active");
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Subscription> getSubscriptionsByUser(int userId) {
        List<Subscription> list = new ArrayList<>();
        String sql = "SELECT * FROM subscriptions WHERE user_id = ? AND status != 'cancelled' ORDER BY renewal_date ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Subscription s = new Subscription();
                    s.setId(rs.getInt("id"));
                    s.setUserId(rs.getInt("user_id"));
                    s.setName(rs.getString("name"));
                    s.setCategory(rs.getString("category"));
                    s.setAmount(rs.getDouble("amount"));
                    s.setBillingCycle(rs.getString("billing_cycle"));
                    if(rs.getDate("renewal_date") != null) {
                        s.setRenewalDate(rs.getDate("renewal_date").toLocalDate());
                    }
                    s.setStatus(rs.getString("status"));
                    s.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean deleteSubscription(int id) {
        String sql = "DELETE FROM subscriptions WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE subscriptions SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Subscription> getUpcomingRenewals(int daysAhead) {
        List<Subscription> list = new ArrayList<>();
        String sql = "SELECT * FROM subscriptions WHERE status = 'active' AND renewal_date >= CURDATE() AND renewal_date <= DATE_ADD(CURDATE(), INTERVAL ? DAY)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, daysAhead);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Subscription s = new Subscription();
                    s.setId(rs.getInt("id"));
                    s.setUserId(rs.getInt("user_id"));
                    s.setName(rs.getString("name"));
                    s.setCategory(rs.getString("category"));
                    s.setAmount(rs.getDouble("amount"));
                    s.setBillingCycle(rs.getString("billing_cycle"));
                    if(rs.getDate("renewal_date") != null) {
                        s.setRenewalDate(rs.getDate("renewal_date").toLocalDate());
                    }
                    s.setStatus(rs.getString("status"));
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateRenewalDate(int id, LocalDate newDate) {
        String sql = "UPDATE subscriptions SET renewal_date = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(newDate));
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

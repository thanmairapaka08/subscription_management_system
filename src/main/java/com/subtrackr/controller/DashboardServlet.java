package com.subtrackr.controller;

import com.subtrackr.dao.SubscriptionDAO;
import com.subtrackr.model.Subscription;
import com.subtrackr.model.User;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private SubscriptionDAO subDAO = new SubscriptionDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("sessionUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("sessionUser");
        
        List<Subscription> subs = subDAO.getSubscriptionsByUser(user.getId());
        
        double totalMonthlySpend = 0;
        int activeCount = 0;
        int upcomingRenewals = 0;
        
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate nextWeek = today.plusDays(7);
        
        for (Subscription s : subs) {
            if ("active".equals(s.getStatus())) {
                activeCount++;
                
                double monthlyAmount = s.getAmount();
                if ("yearly".equals(s.getBillingCycle())) {
                    monthlyAmount = s.getAmount() / 12.0;
                } else if ("weekly".equals(s.getBillingCycle())) {
                    monthlyAmount = s.getAmount() * 4.33;
                }
                totalMonthlySpend += monthlyAmount;
                
                if (s.getRenewalDate() != null && !s.getRenewalDate().isBefore(today) && !s.getRenewalDate().isAfter(nextWeek)) {
                    upcomingRenewals++;
                }
            }
        }

        request.setAttribute("subscriptions", subs);
        request.setAttribute("totalMonthlySpend", totalMonthlySpend);
        request.setAttribute("activeCount", activeCount);
        request.setAttribute("upcomingRenewals", upcomingRenewals);
        
        request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
    }
}

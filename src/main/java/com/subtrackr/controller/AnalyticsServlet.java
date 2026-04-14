package com.subtrackr.controller;

import com.subtrackr.dao.SubscriptionDAO;
import com.subtrackr.dao.UserDAO;
import com.subtrackr.model.Subscription;
import com.subtrackr.model.User;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class AnalyticsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private SubscriptionDAO subDAO = new SubscriptionDAO();
    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("sessionUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User sessionUser = (User) session.getAttribute("sessionUser");
        // refresh user to get latest income
        User user = userDAO.getUserById(sessionUser.getId());
        session.setAttribute("sessionUser", user);

        List<Subscription> subs = subDAO.getSubscriptionsByUser(user.getId());
        
        double totalMonthlySpend = 0;
        Subscription mostExpensive = null;
        double maxCost = 0;
        
        Map<String, Double> categoryData = new HashMap<>();
        
        for (Subscription s : subs) {
            if ("active".equals(s.getStatus())) {
                double monthlyAmount = s.getAmount();
                if ("yearly".equals(s.getBillingCycle())) monthlyAmount /= 12.0;
                else if ("weekly".equals(s.getBillingCycle())) monthlyAmount *= 4.33;
                
                totalMonthlySpend += monthlyAmount;
                
                if (monthlyAmount > maxCost) {
                    maxCost = monthlyAmount;
                    mostExpensive = s;
                }
                
                categoryData.put(s.getCategory(), categoryData.getOrDefault(s.getCategory(), 0.0) + monthlyAmount);
            }
        }
        
        double savingsRatio = 0;
        if (user.getIncome() > 0) {
            savingsRatio = ((user.getIncome() - totalMonthlySpend) / user.getIncome()) * 100.0;
        }

        // Mock historical data for last 6 months based on current spend
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM");
        
        StringBuilder monthlyDataJson = new StringBuilder("{");
        for (int i = 5; i >= 0; i--) {
            String month = today.minusMonths(i).format(formatter);
            double simulatedSpend = totalMonthlySpend * (0.9 + (Math.random() * 0.2)); 
            if(i == 0) simulatedSpend = totalMonthlySpend;
            
            monthlyDataJson.append("\"").append(month).append("\":").append(simulatedSpend);
            if (i > 0) monthlyDataJson.append(",");
        }
        monthlyDataJson.append("}");

        StringBuilder categoryDataJson = new StringBuilder("{");
        int count = 0;
        for (Map.Entry<String, Double> entry : categoryData.entrySet()) {
            categoryDataJson.append("\"").append(entry.getKey()).append("\":").append(entry.getValue());
            count++;
            if (count < categoryData.size()) categoryDataJson.append(",");
        }
        categoryDataJson.append("}");

        request.setAttribute("totalMonthlySpend", totalMonthlySpend);
        request.setAttribute("userIncome", user.getIncome());
        request.setAttribute("savingsRatio", savingsRatio);
        request.setAttribute("mostExpensive", mostExpensive);
        request.setAttribute("monthlyDataJson", monthlyDataJson.toString());
        request.setAttribute("categoryDataJson", categoryDataJson.toString());
        
        request.getRequestDispatcher("/analytics.jsp").forward(request, response);
    }
}

package com.subtrackr.controller;

import com.subtrackr.dao.SubscriptionDAO;
import com.subtrackr.model.Subscription;
import com.subtrackr.model.User;

import java.io.IOException;
import java.time.LocalDate;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class SubscriptionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private SubscriptionDAO subDAO = new SubscriptionDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("sessionUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("sessionUser");
        String action = request.getParameter("action");
        
        if ("add".equals(action)) {
            Subscription s = new Subscription();
            s.setUserId(user.getId());
            s.setName(request.getParameter("name"));
            s.setCategory(request.getParameter("category"));
            s.setAmount(Double.parseDouble(request.getParameter("amount")));
            s.setBillingCycle(request.getParameter("billingCycle"));
            s.setRenewalDate(LocalDate.parse(request.getParameter("renewalDate")));
            s.setStatus("active");
            
            subDAO.addSubscription(s);
            response.sendRedirect(request.getContextPath() + "/dashboard");
            
        } else if ("delete".equals(action)) {
            int subId = Integer.parseInt(request.getParameter("subscriptionId"));
            subDAO.deleteSubscription(subId);
            response.sendRedirect(request.getContextPath() + "/dashboard");
            
        } else if ("pause".equals(action)) {
            int subId = Integer.parseInt(request.getParameter("subscriptionId"));
            subDAO.updateStatus(subId, "paused");
            response.sendRedirect(request.getContextPath() + "/dashboard");
        } else {
            response.sendRedirect(request.getContextPath() + "/dashboard");
        }
    }
}

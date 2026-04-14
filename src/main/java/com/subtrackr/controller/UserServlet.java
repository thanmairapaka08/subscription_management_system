package com.subtrackr.controller;

import com.subtrackr.dao.UserDAO;
import com.subtrackr.model.User;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class UserServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("sessionUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("sessionUser");
        String action = request.getParameter("action");
        
        if ("updateIncome".equals(action)) {
            double income = Double.parseDouble(request.getParameter("income"));
            userDAO.updateIncome(user.getId(), income);
            response.sendRedirect(request.getContextPath() + "/analytics");
        } else {
            response.sendRedirect(request.getContextPath() + "/dashboard");
        }
    }
}

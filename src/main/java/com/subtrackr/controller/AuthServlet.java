package com.subtrackr.controller;

import com.subtrackr.dao.UserDAO;
import com.subtrackr.model.User;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class AuthServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("login".equals(action)) {
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            
            User user = userDAO.loginUser(email, password);
            if (user != null) {
                HttpSession session = request.getSession();
                session.setAttribute("sessionUser", user);
                response.sendRedirect(request.getContextPath() + "/dashboard");
            } else {
                request.setAttribute("errorMessage", "Invalid email or password");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
            }
        } else if ("register".equals(action)) {
            String name = request.getParameter("name");
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            
            User user = new User();
            user.setFullName(name);
            user.setEmail(email);
            user.setPassword(password); // Will be hashed in DAO
            
            if (userDAO.registerUser(user)) {
                response.sendRedirect(request.getContextPath() + "/login.jsp?registered=true");
            } else {
                request.setAttribute("errorMessage", "Registration failed. Email might already exist.");
                request.getRequestDispatcher("/signup.jsp").forward(request, response);
            }
        } else if ("logout".equals(action)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/");
        } else {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("logout".equals(action)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/");
        } else {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
        }
    }
}

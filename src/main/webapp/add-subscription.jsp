<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.subtrackr.model.User" %>
<%  
    if(session.getAttribute("sessionUser") == null) { 
        response.sendRedirect(request.getContextPath() + "/login.jsp"); 
        return; 
    } 
    User user = (User) session.getAttribute("sessionUser");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Add Subscription - SubTrackr</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css">
</head>
<body>
    <nav class="navbar scrolled">
        <div class="container">
            <div style="display: flex; align-items: center; gap: 2rem;">
                <a href="${pageContext.request.contextPath}/" class="nav-brand">SubTrackr</a>
                <ul class="nav-links" style="margin-bottom: 0;">
                    <li><a href="${pageContext.request.contextPath}/dashboard">Dashboard</a></li>
                    <li><a href="${pageContext.request.contextPath}/analytics">Analytics</a></li>
                </ul>
            </div>
            <div class="nav-actions">
                <span style="color: var(--text-muted); margin-right: 1rem;">Hello, <%= user.getFullName() %></span>
                <form action="${pageContext.request.contextPath}/auth" method="POST" style="display:inline;">
                    <input type="hidden" name="action" value="logout">
                    <button type="submit" class="btn btn-ghost" style="padding: 0.5rem 1rem;">Logout</button>
                </form>
            </div>
        </div>
    </nav>

    <div class="dashboard-container">
        <div class="auth-card form-card">
            <h2 style="margin-bottom: 1.5rem;">Add Subscription</h2>
            
            <form action="${pageContext.request.contextPath}/subscription" method="POST">
                <input type="hidden" name="action" value="add">
                
                <div class="form-group">
                    <label for="name">Subscription Name</label>
                    <input type="text" id="name" name="name" class="form-control" placeholder="e.g. Netflix" required>
                </div>

                <div class="form-group">
                    <label for="category">Category</label>
                    <select id="category" name="category" class="form-control" required style="background-color: var(--bg-navy);">
                        <option value="streaming">Streaming</option>
                        <option value="music">Music</option>
                        <option value="productivity">Productivity</option>
                        <option value="fitness">Fitness</option>
                        <option value="education">Education</option>
                        <option value="other">Other</option>
                    </select>
                </div>
                
                <div class="form-group">
                    <label for="amount">Amount (₹)</label>
                    <input type="number" step="0.01" id="amount" name="amount" class="form-control" placeholder="0.00" required>
                </div>

                <div class="form-group">
                    <label for="billingCycle">Billing Cycle</label>
                    <select id="billingCycle" name="billingCycle" class="form-control" required style="background-color: var(--bg-navy);">
                        <option value="monthly">Monthly</option>
                        <option value="yearly">Yearly</option>
                        <option value="weekly">Weekly</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="renewalDate">Next Renewal Date</label>
                    <input type="date" id="renewalDate" name="renewalDate" class="form-control" required style="color-scheme: dark;">
                </div>
                
                <button type="submit" class="btn btn-primary">Add Subscription</button>
                
                <div style="margin-top: 1rem; text-align: center;">
                    <a href="${pageContext.request.contextPath}/dashboard" style="color: var(--text-muted); font-size: 0.875rem;">Cancel</a>
                </div>
            </form>
        </div>
    </div>
</body>
</html>

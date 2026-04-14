<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.subtrackr.model.Subscription" %>
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
    <title>Analytics - SubTrackr</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>
    <nav class="navbar scrolled">
        <div class="container">
            <div style="display: flex; align-items: center; gap: 2rem;">
                <a href="${pageContext.request.contextPath}/" class="nav-brand">SubTrackr</a>
                <ul class="nav-links" style="margin-bottom: 0;">
                    <li><a href="${pageContext.request.contextPath}/dashboard">Dashboard</a></li>
                    <li><a href="${pageContext.request.contextPath}/analytics" style="color: var(--text-white);">Analytics</a></li>
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
        <h2 class="dashboard-header">Analytics & Spending</h2>

        <div class="summary-cards">
            <div class="stat-card">
                <div class="stat-title">Total Monthly Spend</div>
                <div class="stat-value">₹<%= String.format("%.2f", request.getAttribute("totalMonthlySpend") != null ? request.getAttribute("totalMonthlySpend") : 0.0) %></div>
            </div>
            <div class="stat-card">
                <div class="stat-title">
                    Monthly Income 
                    <span id="editIncomeBtn" style="cursor: pointer; margin-left: 0.5rem;" title="Edit Income">✎</span>
                </div>
                <div class="stat-value" id="incomeDisplay">
                    ₹<%= String.format("%.2f", request.getAttribute("userIncome") != null ? request.getAttribute("userIncome") : 0.0) %>
                </div>
                <div id="incomeForm" style="display: none; margin-top: 0.5rem;">
                    <form action="${pageContext.request.contextPath}/user" method="POST" style="display: flex; gap: 0.5rem;">
                        <input type="hidden" name="action" value="updateIncome">
                        <input type="number" name="income" step="0.01" class="form-control" style="padding: 0.25rem 0.5rem;" value="<%= request.getAttribute("userIncome") %>" required>
                        <button type="submit" class="btn btn-primary" style="padding: 0.25rem 0.5rem;">Save</button>
                    </form>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-title">Savings Ratio</div>
                <% 
                    double ratio = request.getAttribute("savingsRatio") != null ? (Double)request.getAttribute("savingsRatio") : 0.0;
                    String color = ratio >= 0 ? "var(--success-emerald)" : "#ef4444";
                %>
                <div class="stat-value" style="color: <%= color %>;">
                    <%= String.format("%.1f", ratio) %>%
                </div>
            </div>
        </div>

        <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 1.5rem; margin-bottom: 2rem;">
            <div class="chart-wrapper">
                <h3 style="margin-bottom: 1rem; color: var(--text-muted); font-size: 1rem;">6 Month Spend Trend</h3>
                <canvas id="monthlySpendChart" style="max-height: 250px;"></canvas>
            </div>
            
            <div style="display: flex; flex-direction: column; gap: 1.5rem;">
                <div class="chart-wrapper" style="height: auto;">
                    <h3 style="margin-bottom: 1rem; color: var(--text-muted); font-size: 1rem;">Spend By Category</h3>
                    <canvas id="categoryPieChart" style="max-height: 200px;"></canvas>
                </div>
                
                <% Subscription mostExpensive = (Subscription) request.getAttribute("mostExpensive"); %>
                <% if (mostExpensive != null) { %>
                    <div class="stat-card" style="margin-bottom: 0;">
                        <div class="stat-title">Most Expensive Subscription</div>
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 1rem;">
                            <div style="font-size: 1.25rem; font-weight: 600;"><%= mostExpensive.getName() %></div>
                            <div style="font-size: 1.25rem; color: var(--accent-indigo); font-weight: bold;">₹<%= String.format("%.2f", mostExpensive.getAmount()) %></div>
                        </div>
                    </div>
                <% } %>
            </div>
        </div>
    </div>

    <!-- Inject data for Chart.js -->
    <script>
        const monthlyData = <%= request.getAttribute("monthlyDataJson") != null ? request.getAttribute("monthlyDataJson") : "{}" %>;
        const categoryData = <%= request.getAttribute("categoryDataJson") != null ? request.getAttribute("categoryDataJson") : "{}" %>;
    </script>
    <script src="${pageContext.request.contextPath}/js/dashboard.js"></script>
</body>
</html>

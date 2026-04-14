<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
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
    <title>Dashboard - SubTrackr</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css">
</head>
<body>
    <!-- Navbar -->
    <nav class="navbar scrolled">
        <div class="container">
            <div style="display: flex; align-items: center; gap: 2rem;">
                <a href="${pageContext.request.contextPath}/" class="nav-brand">SubTrackr</a>
                <ul class="nav-links" style="margin-bottom: 0;">
                    <li><a href="${pageContext.request.contextPath}/dashboard" style="color: var(--text-white);">Dashboard</a></li>
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
        <div class="dashboard-header">
            <h2>Your Subscriptions</h2>
            <a href="${pageContext.request.contextPath}/add-subscription.jsp" class="btn btn-primary">Add Subscription</a>
        </div>

        <div class="summary-cards">
            <div class="stat-card">
                <div class="stat-title">Total Monthly Spend</div>
                <div class="stat-value">₹<%= String.format("%.2f", request.getAttribute("totalMonthlySpend") != null ? request.getAttribute("totalMonthlySpend") : 0.0) %></div>
            </div>
            <div class="stat-card">
                <div class="stat-title">Active Subscriptions</div>
                <div class="stat-value"><%= request.getAttribute("activeCount") != null ? request.getAttribute("activeCount") : 0 %></div>
            </div>
            <div class="stat-card">
                <div class="stat-title">Upcoming Renewals (7 Days)</div>
                <div class="stat-value" style="color: var(--accent-indigo);"><%= request.getAttribute("upcomingRenewals") != null ? request.getAttribute("upcomingRenewals") : 0 %></div>
            </div>
        </div>

        <div class="table-container">
            <% List<Subscription> subs = (List<Subscription>) request.getAttribute("subscriptions"); %>
            <% if (subs != null && !subs.isEmpty()) { %>
                <table class="sub-table">
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Category</th>
                            <th>Amount</th>
                            <th>Billing Cycle</th>
                            <th>Renewal Date</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (Subscription s : subs) { %>
                            <tr>
                                <td style="font-weight: 500;"><%= s.getName() %></td>
                                <td style="text-transform: capitalize; color: var(--text-muted);"><%= s.getCategory() %></td>
                                <td>₹<%= String.format("%.2f", s.getAmount()) %></td>
                                <td style="text-transform: capitalize;"><%= s.getBillingCycle() %></td>
                                <td><%= s.getRenewalDate() %></td>
                                <td>
                                    <span class="status-badge status-<%= s.getStatus().toLowerCase() %>"><%= s.getStatus() %></span>
                                </td>
                                <td>
                                    <div style="display: flex; gap: 0.5rem;">
                                        <% if ("active".equals(s.getStatus())) { %>
                                            <form action="${pageContext.request.contextPath}/subscription" method="POST">
                                                <input type="hidden" name="action" value="pause">
                                                <input type="hidden" name="subscriptionId" value="<%= s.getId() %>">
                                                <button type="submit" class="action-btn" title="Pause">⏸</button>
                                            </form>
                                        <% } %>
                                        <form action="${pageContext.request.contextPath}/subscription" method="POST" onsubmit="return confirm('Are you sure you want to delete this subscription?');">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="subscriptionId" value="<%= s.getId() %>">
                                            <button type="submit" class="action-btn" style="color: #ef4444;" title="Delete">🗑</button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            <% } else { %>
                <div class="empty-state">
                    <div style="font-size: 3rem; margin-bottom: 1rem;">📦</div>
                    <p>No subscriptions yet. Add your first one!</p>
                </div>
            <% } %>
        </div>
    </div>
</body>
</html>

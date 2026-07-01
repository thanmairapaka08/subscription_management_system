package com.subtrackr.controller;

import com.subtrackr.model.Subscription;
import com.subtrackr.model.User;
import com.subtrackr.service.RecommendationService;
import com.subtrackr.service.SubscriptionService;
import com.subtrackr.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AnalyticsController {

    private final SubscriptionService subscriptionService;
    private final UserService userService;
    private final RecommendationService recommendationService;

    @Autowired
    public AnalyticsController(SubscriptionService subscriptionService,
                               UserService userService,
                               RecommendationService recommendationService) {
        this.subscriptionService = subscriptionService;
        this.userService = userService;
        this.recommendationService = recommendationService;
    }

    @GetMapping("/analytics")
    public String standardAnalytics(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            return "redirect:/login";
        }

        if ("premium".equalsIgnoreCase(sessionUser.getPlan())) {
            return "redirect:/premium-analytics";
        }

        User user = userService.getUserById(sessionUser.getId());
        if (user != null) {
            session.setAttribute("sessionUser", user);
        } else {
            user = sessionUser;
        }

        List<Subscription> subs = subscriptionService.getSubscriptionsByUser(user.getId());

        double totalMonthlySpend = 0;
        Map<String, Double> categoryData = new HashMap<>();

        for (Subscription s : subs) {
            if ("active".equalsIgnoreCase(s.getStatus())) {
                double monthlyAmount = s.getAmount();
                if ("yearly".equalsIgnoreCase(s.getBillingCycle())) {
                    monthlyAmount = s.getAmount() / 12.0;
                } else if ("weekly".equalsIgnoreCase(s.getBillingCycle())) {
                    monthlyAmount = s.getAmount() * 4.33;
                }

                totalMonthlySpend += monthlyAmount;
                categoryData.put(s.getCategory(), categoryData.getOrDefault(s.getCategory(), 0.0) + monthlyAmount);
            }
        }

        double savingsRatio = 0;
        if (user.getIncome() > 0) {
            savingsRatio = ((user.getIncome() - totalMonthlySpend) / user.getIncome()) * 100.0;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM");
        LocalDate today = LocalDate.now();

        JSONArray monthlyLabelsJson = new JSONArray();
        JSONArray monthlyValuesJson = new JSONArray();

        for (int i = 5; i >= 0; i--) {
            LocalDate targetDate = today.minusMonths(i);
            YearMonth targetMonth = YearMonth.from(targetDate);
            String monthLabel = targetDate.format(formatter);

            double simulatedSpend = 0;
            for (Subscription s : subs) {
                if (!"cancelled".equalsIgnoreCase(s.getStatus())) {
                    Timestamp createdAt = s.getCreatedAt();
                    if (createdAt != null) {
                        YearMonth createdMonth = YearMonth.from(createdAt.toLocalDateTime().toLocalDate());
                        if (!createdMonth.isAfter(targetMonth)) {
                            double amt = s.getAmount();
                            if ("yearly".equalsIgnoreCase(s.getBillingCycle())) amt /= 12.0;
                            else if ("weekly".equalsIgnoreCase(s.getBillingCycle())) amt *= 4.33;
                            simulatedSpend += amt;
                        }
                    } else {
                        double amt = s.getAmount();
                        if ("yearly".equalsIgnoreCase(s.getBillingCycle())) amt /= 12.0;
                        else if ("weekly".equalsIgnoreCase(s.getBillingCycle())) amt *= 4.33;
                        simulatedSpend += amt;
                    }
                }
            }

            monthlyLabelsJson.put(monthLabel);
            monthlyValuesJson.put(simulatedSpend);
        }

        JSONArray categoryLabelsJson = new JSONArray();
        JSONArray categoryValuesJson = new JSONArray();
        for (Map.Entry<String, Double> entry : categoryData.entrySet()) {
            categoryLabelsJson.put(entry.getKey());
            categoryValuesJson.put(entry.getValue());
        }

        model.addAttribute("totalMonthlySpend", totalMonthlySpend);
        model.addAttribute("userIncome", user.getIncome());
        model.addAttribute("savingsRatio", savingsRatio);

        model.addAttribute("monthlyLabelsJson", monthlyLabelsJson.toString());
        model.addAttribute("monthlyValuesJson", monthlyValuesJson.toString());
        model.addAttribute("categoryLabelsJson", categoryLabelsJson.toString());
        model.addAttribute("categoryValuesJson", categoryValuesJson.toString());

        return "analytics";
    }

    @GetMapping("/premium-analytics")
    public String premiumAnalytics(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            return "redirect:/login";
        }

        if ("basic".equalsIgnoreCase(sessionUser.getPlan())) {
            return "redirect:/analytics";
        }

        User user = userService.getUserById(sessionUser.getId());
        if (user != null) {
            session.setAttribute("sessionUser", user);
        } else {
            user = sessionUser;
        }

        List<Subscription> subs = subscriptionService.getSubscriptionsByUser(user.getId());

        double totalMonthlySpend = 0;
        Map<String, Double> categoryData = new HashMap<>();
        Map<Integer, List<String>> calendarData = new HashMap<>();

        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();

        for (Subscription s : subs) {
            if ("active".equalsIgnoreCase(s.getStatus())) {
                double monthlyAmount = s.getAmount();
                if ("yearly".equalsIgnoreCase(s.getBillingCycle())) monthlyAmount /= 12.0;
                else if ("weekly".equalsIgnoreCase(s.getBillingCycle())) monthlyAmount *= 4.33;

                totalMonthlySpend += monthlyAmount;

                categoryData.put(s.getCategory(), categoryData.getOrDefault(s.getCategory(), 0.0) + monthlyAmount);

                if (s.getRenewalDate() != null) {
                    if (s.getRenewalDate().getMonthValue() == currentMonth || "monthly".equalsIgnoreCase(s.getBillingCycle())) {
                        int dayOfMonth = s.getRenewalDate().getDayOfMonth();
                        calendarData.computeIfAbsent(dayOfMonth, k -> new ArrayList<>()).add(s.getName());
                    }
                }
            }
        }

        double yearlyProjection = totalMonthlySpend * 12;

        double savingsRatio = 0;
        if (user.getIncome() > 0) {
            savingsRatio = ((user.getIncome() - totalMonthlySpend) / user.getIncome()) * 100.0;
        }

        int healthScore = 100;
        int activeCount = subscriptionService.countActiveSubscriptions(user.getId());
        healthScore -= (activeCount * 5);
        if (user.getIncome() > 0) {
            double spendRatio = totalMonthlySpend / user.getIncome();
            if (spendRatio > 0.30) healthScore -= 30;
            else if (spendRatio > 0.20) healthScore -= 20;
        }
        healthScore = Math.max(0, healthScore);

        String healthLabel = "Excellent";
        String healthColor = "var(--success-emerald)";
        if (healthScore < 40) {
            healthLabel = "Overspending";
            healthColor = "#EF4444";
        } else if (healthScore < 60) {
            healthLabel = "Fair";
            healthColor = "#F97316";
        } else if (healthScore < 80) {
            healthLabel = "Good";
            healthColor = "#EAB308";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM");
        double maxMonthSpend = 0;
        String biggestMonth = "";

        JSONArray monthlyLabelsJson = new JSONArray();
        JSONArray monthlyValuesJson = new JSONArray();

        for (int i = 11; i >= 0; i--) {
            LocalDate targetDate = today.minusMonths(i);
            YearMonth targetMonth = YearMonth.from(targetDate);
            String monthLabel = targetDate.format(formatter);

            double simulatedSpend = 0;

            for (Subscription s : subs) {
                if (!"cancelled".equalsIgnoreCase(s.getStatus())) {
                    Timestamp createdAt = s.getCreatedAt();
                    if (createdAt != null) {
                        YearMonth createdMonth = YearMonth.from(createdAt.toLocalDateTime().toLocalDate());
                        if (!createdMonth.isAfter(targetMonth)) {
                            double amt = s.getAmount();
                            if ("yearly".equalsIgnoreCase(s.getBillingCycle())) amt /= 12.0;
                            else if ("weekly".equalsIgnoreCase(s.getBillingCycle())) amt *= 4.33;
                            simulatedSpend += amt;
                        }
                    } else {
                        double amt = s.getAmount();
                        if ("yearly".equalsIgnoreCase(s.getBillingCycle())) amt /= 12.0;
                        else if ("weekly".equalsIgnoreCase(s.getBillingCycle())) amt *= 4.33;
                        simulatedSpend += amt;
                    }
                }
            }

            if (simulatedSpend > maxMonthSpend) {
                maxMonthSpend = simulatedSpend;
                biggestMonth = monthLabel + " (₹" + String.format("%.2f", simulatedSpend) + ")";
            }

            monthlyLabelsJson.put(monthLabel);
            monthlyValuesJson.put(simulatedSpend);
        }

        List<String> categoryAlerts = new ArrayList<>();
        JSONArray categoryLabelsJson = new JSONArray();
        JSONArray categoryValuesJson = new JSONArray();

        for (Map.Entry<String, Double> entry : categoryData.entrySet()) {
            if (entry.getValue() > 500) {
                categoryAlerts.add("High spending detected in " + entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1) + " — ₹" + String.format("%.2f", entry.getValue()) + "/month");
            }
            categoryLabelsJson.put(entry.getKey());
            categoryValuesJson.put(entry.getValue());
        }

        StringBuilder calendarDataJson = new StringBuilder("{");
        int count = 0;
        for (Map.Entry<Integer, List<String>> entry : calendarData.entrySet()) {
            calendarDataJson.append("\"").append(entry.getKey()).append("\":[");
            for (int i = 0; i < entry.getValue().size(); i++) {
                calendarDataJson.append("\"").append(entry.getValue().get(i)).append("\"");
                if (i < entry.getValue().size() - 1) calendarDataJson.append(",");
            }
            calendarDataJson.append("]");
            count++;
            if (count < calendarData.size()) calendarDataJson.append(",");
        }
        calendarDataJson.append("}");

        List<String> recommendations = recommendationService.generateRecommendations(subs, user.getIncome());

        model.addAttribute("totalMonthlySpend", totalMonthlySpend);
        model.addAttribute("yearlyProjection", yearlyProjection);
        model.addAttribute("userIncome", user.getIncome());
        model.addAttribute("savingsRatio", savingsRatio);
        model.addAttribute("healthScore", healthScore);
        model.addAttribute("healthLabel", healthLabel);
        model.addAttribute("healthColor", healthColor);
        model.addAttribute("biggestMonth", biggestMonth);
        model.addAttribute("categoryAlerts", categoryAlerts);
        model.addAttribute("recommendations", recommendations);

        model.addAttribute("monthlyLabelsJson", monthlyLabelsJson.toString());
        model.addAttribute("monthlyValuesJson", monthlyValuesJson.toString());
        model.addAttribute("categoryLabelsJson", categoryLabelsJson.toString());
        model.addAttribute("categoryValuesJson", categoryValuesJson.toString());

        model.addAttribute("calendarDataJson", calendarDataJson.toString());

        return "premium-analytics";
    }
}

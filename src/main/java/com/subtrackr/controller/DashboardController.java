package com.subtrackr.controller;

import com.subtrackr.model.Subscription;
import com.subtrackr.model.User;
import com.subtrackr.service.SubscriptionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class DashboardController {

    private final SubscriptionService subscriptionService;

    @Autowired
    public DashboardController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/dashboard")
    public String viewDashboard(HttpSession session, Model model,
                                @RequestParam(value = "error", required = false) String error) {
        User user = (User) session.getAttribute("sessionUser");
        if (user == null) {
            return "redirect:/login";
        }

        List<Subscription> subs = subscriptionService.getSubscriptionsByUser(user.getId());

        double totalMonthlySpend = 0;
        int activeCount = 0;
        int upcomingRenewals = 0;

        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);

        for (Subscription s : subs) {
            if ("active".equalsIgnoreCase(s.getStatus())) {
                activeCount++;

                double monthlyAmount = s.getAmount();
                if ("yearly".equalsIgnoreCase(s.getBillingCycle())) {
                    monthlyAmount = s.getAmount() / 12.0;
                } else if ("weekly".equalsIgnoreCase(s.getBillingCycle())) {
                    monthlyAmount = s.getAmount() * 4.33;
                }
                totalMonthlySpend += monthlyAmount;

                if (s.getRenewalDate() != null && !s.getRenewalDate().isBefore(today) && !s.getRenewalDate().isAfter(nextWeek)) {
                    upcomingRenewals++;
                }
            }
        }

        if (error != null) {
            if ("limit_reached".equals(error)) {
                model.addAttribute("errorMessage", "Active subscription limit reached for Basic plan (max 4). Upgrade to Premium for unlimited subscriptions.");
            } else if ("invalid".equals(error)) {
                model.addAttribute("errorMessage", "Invalid action parameters.");
            }
        }

        model.addAttribute("subscriptions", subs);
        model.addAttribute("totalMonthlySpend", totalMonthlySpend);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("upcomingRenewals", upcomingRenewals);
        model.addAttribute("subscriptionCount", activeCount);
        model.addAttribute("userPlan", user.getPlan());

        return "dashboard";
    }
}

package com.subtrackr.controller;

import com.subtrackr.model.User;
import com.subtrackr.service.SubscriptionService;
import com.subtrackr.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    private final UserService userService;
    private final SubscriptionService subscriptionService;

    @Autowired
    public UserController(UserService userService, SubscriptionService subscriptionService) {
        this.userService = userService;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/profile")
    public String viewProfile(HttpSession session, org.springframework.ui.Model model) {
        User user = (User) session.getAttribute("sessionUser");
        if (user == null) {
            return "redirect:/login";
        }
        int subCount = subscriptionService.getSubscriptionsByUser(user.getId()).size();
        model.addAttribute("subCount", subCount);
        return "profile";
    }

    @GetMapping("/settings")
    public String viewSettings(HttpSession session) {
        if (session.getAttribute("sessionUser") == null) {
            return "redirect:/login";
        }
        return "settings";
    }

    @PostMapping("/settings")
    public String handleSettingsPost(
            @RequestParam("action") String action,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "currentPassword", required = false) String currentPassword,
            @RequestParam(value = "newPassword", required = false) String newPassword,
            HttpSession session,
            org.springframework.ui.Model model) {

        User user = (User) session.getAttribute("sessionUser");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            if ("updateProfile".equals(action)) {
                User updated = userService.updateProfile(user.getId(), name, email);
                session.setAttribute("sessionUser", updated);
                model.addAttribute("successMessage", "Profile updated successfully!");
            } else if ("updatePassword".equals(action)) {
                userService.updatePassword(user.getId(), currentPassword, newPassword);
                model.addAttribute("successMessage", "Password updated successfully!");
            } else if ("toggleReminders".equals(action)) {
                model.addAttribute("successMessage", "Reminder preference saved!");
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while updating settings.");
            e.printStackTrace();
        }

        return "settings";
    }

    @PostMapping("/user")
    public String handleUserAction(
            @RequestParam("action") String action,
            @RequestParam(value = "income", required = false) Double income,
            @RequestParam(value = "redirect", required = false) String redirectTarget,
            HttpSession session) {

        User user = (User) session.getAttribute("sessionUser");
        if (user == null) {
            return "redirect:/login";
        }

        if ("updateIncome".equals(action)) {
            if (income != null) {
                userService.updateIncome(user.getId(), income);
                user.setIncome(income);
                session.setAttribute("sessionUser", user);
            }
            if ("premium-analytics".equals(redirectTarget)) {
                return "redirect:/premium-analytics";
            } else {
                return "redirect:/analytics";
            }
        } else if ("downgradePlan".equals(action)) {
            int activeCount = subscriptionService.countActiveSubscriptions(user.getId());
            if (activeCount > 4) {
                return "redirect:/dashboard?error=too_many_subs";
            }

            userService.updatePlan(user.getId(), "basic");
            user.setPlan("basic");
            session.setAttribute("sessionUser", user);

            return "redirect:/dashboard?success=downgraded";
        }

        return "redirect:/dashboard";
    }
}

package com.subtrackr.controller;

import com.subtrackr.model.User;
import com.subtrackr.service.PaymentService;
import com.subtrackr.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AdminController {

    private final UserService userService;
    private final PaymentService paymentService;

    @Autowired
    public AdminController(UserService userService, PaymentService paymentService) {
        this.userService = userService;
        this.paymentService = paymentService;
    }

    @GetMapping("/admin")
    public String adminDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("sessionUser");
        if (user == null) {
            return "redirect:/login";
        }

        // Admin restriction check
        if (!"admin".equalsIgnoreCase(user.getRole())) {
            return "redirect:/dashboard";
        }

        List<User> users = userService.getAllUsers();
        int totalUsers = users.size();
        int premiumUsers = 0;
        int basicUsers = 0;

        for (User u : users) {
            if ("premium".equalsIgnoreCase(u.getPlan())) {
                premiumUsers++;
            } else {
                basicUsers++;
            }
        }

        double totalRevenue = paymentService.getTotalRevenue();

        model.addAttribute("users", users);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("premiumUsers", premiumUsers);
        model.addAttribute("basicUsers", basicUsers);
        model.addAttribute("totalRevenue", totalRevenue);

        return "admin";
    }

    @PostMapping("/admin/user/plan")
    public String toggleUserPlan(@RequestParam("userId") int userId, HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null || !"admin".equalsIgnoreCase(sessionUser.getRole())) {
            return "redirect:/login";
        }

        User targetUser = userService.getUserById(userId);
        if (targetUser != null) {
            String newPlan = "premium".equalsIgnoreCase(targetUser.getPlan()) ? "basic" : "premium";
            userService.updatePlan(userId, newPlan);
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/user/status")
    public String toggleUserStatus(@RequestParam("userId") int userId, HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null || !"admin".equalsIgnoreCase(sessionUser.getRole())) {
            return "redirect:/login";
        }

        User targetUser = userService.getUserById(userId);
        if (targetUser != null) {
            String newStatus = "active".equalsIgnoreCase(targetUser.getStatus()) ? "suspended" : "active";
            userService.updateStatus(userId, newStatus);
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/user/delete")
    public String deleteUser(@RequestParam("userId") int userId, HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null || !"admin".equalsIgnoreCase(sessionUser.getRole())) {
            return "redirect:/login";
        }

        if (sessionUser.getId() == userId) {
            return "redirect:/admin?error=self_delete";
        }

        userService.deleteUser(userId);
        return "redirect:/admin?success=user_deleted";
    }
}

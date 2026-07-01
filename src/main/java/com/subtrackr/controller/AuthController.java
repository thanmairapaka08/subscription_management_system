package com.subtrackr.controller;

import com.subtrackr.model.User;
import com.subtrackr.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "registered", required = false) String registered, Model model) {
        if (registered != null) {
            model.addAttribute("successMessage", "Registration successful! Please login.");
        }
        return "login";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @PostMapping("/auth")
    public String handleAuthPost(
            @RequestParam("action") String action,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "name", required = false) String name,
            HttpSession session,
            Model model) {

        if ("login".equals(action)) {
            User user = userService.loginUser(email != null ? email.trim() : "", password != null ? password.trim() : "");
            if (user != null) {
                if ("suspended".equalsIgnoreCase(user.getStatus())) {
                    model.addAttribute("errorMessage", "Your account is suspended. Please contact administrator.");
                    return "login";
                }
                session.setAttribute("sessionUser", user);
                return "redirect:/dashboard";
            } else {
                model.addAttribute("errorMessage", "Invalid email or password");
                return "login";
            }
        } else if ("register".equals(action)) {
            User user = new User();
            user.setFullName(name);
            user.setEmail(email);
            user.setPassword(password);

            try {
                userService.registerUser(user);
                return "redirect:/login?registered=true";
            } catch (Exception e) {
                model.addAttribute("errorMessage", "Registration failed. " + e.getMessage());
                return "signup";
            }
        } else if ("logout".equals(action)) {
            session.invalidate();
            return "redirect:/";
        }
        return "redirect:/";
    }

    @GetMapping("/auth")
    public String handleAuthGet(@RequestParam("action") String action, HttpSession session) {
        if ("logout".equals(action)) {
            session.invalidate();
        }
        return "redirect:/";
    }
}

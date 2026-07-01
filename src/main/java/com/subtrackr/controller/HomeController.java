package com.subtrackr.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/", "/home"})
    public String home() {
        return "index";
    }

    @GetMapping("/notifications")
    public String notifications(HttpSession session) {
        if (session.getAttribute("sessionUser") == null) {
            return "redirect:/login";
        }
        return "notifications";
    }
}

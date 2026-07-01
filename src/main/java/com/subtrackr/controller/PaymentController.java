package com.subtrackr.controller;

import com.subtrackr.model.Payment;
import com.subtrackr.model.User;
import com.subtrackr.service.PaymentService;
import com.subtrackr.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;

    @Autowired
    public PaymentController(PaymentService paymentService, UserService userService) {
        this.paymentService = paymentService;
        this.userService = userService;
    }

    @GetMapping
    public String showPaymentHistory(HttpSession session, Model model) {
        User user = (User) session.getAttribute("sessionUser");
        if (user == null) {
            return "redirect:/login";
        }

        List<Payment> payments = paymentService.getPaymentsByUser(user.getId());
        model.addAttribute("payments", payments);
        return "payment-history";
    }

    @PostMapping
    public String processPayment(
            @RequestParam("action") String action,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("sessionUser");
        if (user == null) {
            return "redirect:/login";
        }

        if ("upgrade".equals(action)) {
            // Simulate Payment
            Payment payment = new Payment();
            payment.setUserId(user.getId());
            payment.setOrderId("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            payment.setPaymentId("PAY-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
            payment.setAmount(9.99); // Monthly premium amount simulation
            payment.setPlan("premium");
            payment.setStatus("Success");

            try {
                paymentService.addPayment(payment);
                userService.updatePlan(user.getId(), "premium");

                // Update session
                user.setPlan("premium");
                session.setAttribute("sessionUser", user);

                model.addAttribute("successMessage", "Payment Successful! You are now a Premium user.");
                
                // Fetch updated payment list
                List<Payment> payments = paymentService.getPaymentsByUser(user.getId());
                model.addAttribute("payments", payments);
                return "payment-history";
            } catch (Exception e) {
                model.addAttribute("errorMessage", "Payment failed to process. Try again.");
                return "upgrade";
            }
        }

        return "redirect:/dashboard";
    }
}

package com.subtrackr.controller;

import com.subtrackr.model.Subscription;
import com.subtrackr.model.User;
import com.subtrackr.service.SubscriptionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/subscription")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/add")
    public String addSubscriptionForm(HttpSession session) {
        if (session.getAttribute("sessionUser") == null) {
            return "redirect:/login";
        }
        return "add-subscription";
    }

    @PostMapping
    public String handleSubscriptionAction(
            @RequestParam("action") String action,
            @RequestParam(value = "subscriptionId", required = false) Integer subscriptionId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "amount", required = false) Double amount,
            @RequestParam(value = "billingCycle", required = false) String billingCycle,
            @RequestParam(value = "renewalDate", required = false) String renewalDate,
            @RequestParam(value = "currentStatus", required = false) String currentStatus,
            HttpSession session) {

        User user = (User) session.getAttribute("sessionUser");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            if ("add".equals(action)) {
                Subscription s = new Subscription();
                s.setUserId(user.getId());
                s.setName(name);
                s.setCategory(category);
                s.setAmount(amount != null ? amount : 0.0);
                s.setBillingCycle(billingCycle);
                s.setRenewalDate(LocalDate.parse(renewalDate));
                s.setStatus("active");

                subscriptionService.addSubscription(s);
            } else if ("delete".equals(action)) {
                if (subscriptionId == null) {
                    return "redirect:/dashboard?error=invalid";
                }
                subscriptionService.deleteSubscription(subscriptionId);
            } else if ("pause".equals(action)) {
                if (subscriptionId == null) {
                    return "redirect:/dashboard?error=invalid";
                }
                if ("paused".equalsIgnoreCase(currentStatus)) {
                    subscriptionService.updateStatus(subscriptionId, "active");
                } else {
                    subscriptionService.updateStatus(subscriptionId, "paused");
                }
            }
        } catch (IllegalStateException e) {
            if ("limit_reached".equals(e.getMessage())) {
                return "redirect:/dashboard?error=limit_reached";
            }
            return "redirect:/dashboard?error=invalid";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/dashboard?error=invalid";
        }

        return "redirect:/dashboard";
    }
}

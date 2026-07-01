package com.subtrackr.controller;

import com.subtrackr.model.User;
import com.subtrackr.service.PaymentService;
import com.subtrackr.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Controller
public class PremiumController {

    private static final String RAZORPAY_KEY_ID = "rzp_test_SeRfXnv1DSc0sA";
    private static final String RAZORPAY_KEY_SECRET = "0abUwbgcY9Y4jkkBdJGixkAL";

    private final PaymentService paymentService;
    private final UserService userService;

    @Autowired
    public PremiumController(PaymentService paymentService, UserService userService) {
        this.paymentService = paymentService;
        this.userService = userService;
    }

    @GetMapping("/upgrade")
    public String upgradePage(HttpSession session) {
        if (session.getAttribute("sessionUser") == null) {
            return "redirect:/login";
        }
        return "upgrade";
    }

    @GetMapping("/payment-success")
    public String paymentSuccess(HttpSession session) {
        if (session.getAttribute("sessionUser") == null) {
            return "redirect:/login";
        }
        return "payment-success";
    }

    @GetMapping("/payment-failed")
    public String paymentFailed(HttpSession session) {
        if (session.getAttribute("sessionUser") == null) {
            return "redirect:/login";
        }
        return "payment-failed";
    }

    @PostMapping(value = "/premium", params = "action=createOrder")
    public ResponseEntity<String> createOrder(HttpSession session) {
        User user = (User) session.getAttribute("sessionUser");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        System.out.println("createOrder called for userId: " + user.getId());
        try {
            URL url = new URL("https://api.razorpay.com/v1/orders");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            String auth = RAZORPAY_KEY_ID + ":" + RAZORPAY_KEY_SECRET;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
            conn.setDoOutput(true);

            int amount = 14900;
            String receipt = "subtrackr_premium_" + user.getId() + "_" + System.currentTimeMillis();

            String jsonInputString = "{\"amount\": " + amount + ", \"currency\": \"INR\", \"receipt\": \"" + receipt + "\"}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            if (conn.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder jsonResponse = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    jsonResponse.append(responseLine.trim());
                }

                System.out.println("Razorpay response: " + jsonResponse.toString());

                JSONObject json = new JSONObject(jsonResponse.toString());
                String orderId = json.getString("id");

                System.out.println("Order ID extracted: " + orderId);

                paymentService.createPayment(user.getId(), orderId, amount, "premium");

                String result = "{\"orderId\":\"" + orderId + "\",\"amount\":14900,\"currency\":\"INR\"}";
                return ResponseEntity.ok()
                        .header("Content-Type", "application/json; charset=UTF-8")
                        .body(result);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create order");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Exception during order creation");
        }
    }

    @PostMapping(value = "/premium", params = "action=verifyPayment")
    public String verifyPayment(
            @RequestParam("razorpay_order_id") String razorpayOrderId,
            @RequestParam("razorpay_payment_id") String razorpayPaymentId,
            @RequestParam(value = "razorpay_signature", required = false) String razorpaySignature,
            HttpSession session) {

        User user = (User) session.getAttribute("sessionUser");
        if (user == null) {
            return "redirect:/login";
        }

        if (razorpayOrderId != null && !razorpayOrderId.isEmpty() && razorpayPaymentId != null && !razorpayPaymentId.isEmpty()) {
            paymentService.updatePaymentStatus(razorpayOrderId, razorpayPaymentId, "paid");
            userService.updatePlan(user.getId(), "premium");

            // Update session
            user.setPlan("premium");
            session.setAttribute("sessionUser", user);

            return "redirect:/payment-success";
        } else {
            return "redirect:/payment-failed";
        }
    }
}

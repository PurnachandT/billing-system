package com.credx.billing.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    @Value("${razorpay.key}")
    private String key;

    @Value("${razorpay.secret}")
    private String secret;

    public boolean processPayment(Long orgId, int amount) {

        try {
            RazorpayClient client = new RazorpayClient(key, secret);

            JSONObject options = new JSONObject();
            options.put("amount", amount * 100); // convert to paise
            options.put("currency", "INR");
            options.put("receipt", "org_" + orgId);

            Order order = client.orders.create(options);

            System.out.println("Order Created: " + order);

            return true;

        } catch (Exception e) {
            System.out.println("Payment failed: " + e.getMessage());
            return false;
        }
    }
}
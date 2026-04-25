package com.credx.billing.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String razorpayCustomerId;

    private LocalDate billingStartDate;

    private LocalDate nextBillingDate;

    private boolean paymentBlocked;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        ACTIVE,
        PAYMENT_FAILED
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRazorpayCustomerId() {
        return razorpayCustomerId;
    }

    public void setRazorpayCustomerId(String razorpayCustomerId) {
        this.razorpayCustomerId = razorpayCustomerId;
    }

    public LocalDate getBillingStartDate() {
        return billingStartDate;
    }

    public void setBillingStartDate(LocalDate billingStartDate) {
        this.billingStartDate = billingStartDate;
    }

    public LocalDate getNextBillingDate() {
        return nextBillingDate;
    }

    public void setNextBillingDate(LocalDate nextBillingDate) {
        this.nextBillingDate = nextBillingDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isPaymentBlocked() {
        return paymentBlocked;
    }

    public void setPaymentBlocked(boolean paymentBlocked) {
        this.paymentBlocked = paymentBlocked;
    }

}
package com.credx.billing.dto;

public class BillingResponse {

    private int totalSeats;
    private int pricePerSeat;
    private int totalAmount;

    public BillingResponse(int totalSeats, int pricePerSeat, int totalAmount) {
        this.totalSeats = totalSeats;
        this.pricePerSeat = pricePerSeat;
        this.totalAmount = totalAmount;
    }

    public int getTotalSeats() { return totalSeats; }
    public int getPricePerSeat() { return pricePerSeat; }
    public int getTotalAmount() { return totalAmount; }
}
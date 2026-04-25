package com.credx.billing.service;

import com.credx.billing.dto.BillingResponse;
import com.credx.billing.model.Invoice;
import com.credx.billing.model.Organization;
import com.credx.billing.model.Seat;
import com.credx.billing.repository.InvoiceRepository;
import com.credx.billing.repository.OrganizationRepository;
import com.credx.billing.repository.SeatRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrganizationService {

    private final OrganizationRepository repository;
    private final SeatRepository seatRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentService paymentService;

    public OrganizationService(
            OrganizationRepository repository,
            SeatRepository seatRepository,
            InvoiceRepository invoiceRepository,
            PaymentService paymentService) {
        this.repository = repository;
        this.seatRepository = seatRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentService = paymentService;
    }

    // Create Organization
    public Organization createOrganization(String name) {

        Organization org = new Organization();
        org.setName(name);
        org.setBillingStartDate(LocalDate.now());
        org.setNextBillingDate(LocalDate.now().plusMonths(1));
        org.setStatus(Organization.Status.ACTIVE);
        org.setPaymentBlocked(false);

        Organization savedOrg = repository.save(org);

        // default seat
        Seat seat = new Seat();
        seat.setOrgId(savedOrg.getId());
        seat.setUserEmail("admin@credx.com");
        seat.setType("BASIC");

        seatRepository.save(seat);

        return savedOrg;
    }

    // Add Seat with Proration
    public void addSeat(Long orgId, String email, String type) {

        Organization org = repository.findById(orgId).orElseThrow();

        if (org.isPaymentBlocked()) {
            throw new RuntimeException("Payment pending. Cannot add seats.");
        }

        int price = getPriceByType(type);

        int totalDays = getTotalDaysInCycle(org);
        int remainingDays = getRemainingDays(org);

        double proratedAmount = price * ((double) remainingDays / totalDays);
        int finalAmount = (int) Math.round(proratedAmount);

        boolean paymentSuccess = paymentService.processPayment(orgId, finalAmount);

        if (!paymentSuccess) {

            Invoice failedInvoice = new Invoice();
            failedInvoice.setOrgId(orgId);
            failedInvoice.setSeatCount(1);
            failedInvoice.setAmount(finalAmount);
            failedInvoice.setSeatType(type);
            failedInvoice.setProrated(true);
            failedInvoice.setStatus("FAILED");
            failedInvoice.setBillingDate(LocalDate.now());

            invoiceRepository.save(failedInvoice);

            org.setPaymentBlocked(true);
            repository.save(org);

            throw new RuntimeException("Payment failed");
        }

        // success → add seat
        Seat seat = new Seat();
        seat.setOrgId(orgId);
        seat.setUserEmail(email);
        seat.setType(type);
        seatRepository.save(seat);

        // save invoice
        Invoice invoice = new Invoice();
        invoice.setOrgId(orgId);
        invoice.setSeatCount(1);
        invoice.setAmount(finalAmount);
        invoice.setSeatType(type);
        invoice.setProrated(true);
        invoice.setStatus("SUCCESS");
        invoice.setBillingDate(LocalDate.now());

        invoiceRepository.save(invoice);
    }

    //  Remove Seat
    public void removeSeat(Long seatId) {
        Seat seat = seatRepository.findById(seatId).orElseThrow();
        seat.setActive(false);
        seatRepository.save(seat);
    }

    // Seat Breakdown
    public Map<String, Integer> getSeatBreakdown(Long orgId) {

        List<Seat> seats = seatRepository.findByOrgIdAndActiveTrue(orgId);
        Map<String, Integer> map = new HashMap<>();

        for (Seat seat : seats) {
            String type = seat.getType() == null ? "BASIC" : seat.getType();
            map.put(type, map.getOrDefault(type, 0) + 1);
        }

        return map;
    }

    // Billing Calculation
    public BillingResponse calculateBill(Long orgId) {

        List<Seat> seats = seatRepository.findByOrgIdAndActiveTrue(orgId);

        int totalSeats = 0;
        int totalAmount = 0;

        for (Seat seat : seats) {
            totalAmount += getPriceByType(seat.getType());
            totalSeats++;
        }

        return new BillingResponse(totalSeats, 0, totalAmount);
    }

    // Monthly Billing
    public BillingResponse generateMonthlyBill(Long orgId) {

        Organization org = repository.findById(orgId).orElseThrow();

        if (org.getNextBillingDate().isAfter(LocalDate.now())) {
            throw new RuntimeException("Billing date not reached yet");
        }

        List<Seat> seats = seatRepository.findByOrgIdAndActiveTrue(orgId);

        int totalSeats = 0;
        int totalAmount = 0;

        for (Seat seat : seats) {
            totalAmount += getPriceByType(seat.getType());
            totalSeats++;
        }

        Invoice invoice = new Invoice();
        invoice.setOrgId(orgId);
        invoice.setSeatCount(totalSeats);
        invoice.setAmount(totalAmount);
        invoice.setSeatType("ALL");
        invoice.setProrated(false);
        invoice.setStatus("SUCCESS");
        invoice.setBillingDate(LocalDate.now());

        invoiceRepository.save(invoice);

        org.setNextBillingDate(org.getNextBillingDate().plusMonths(1));
        repository.save(org);

        return new BillingResponse(totalSeats, 0, totalAmount);
    }

    // Billing History
    public List<Invoice> getBillingHistory(Long orgId) {
        return invoiceRepository.findByOrgIdOrderByBillingDateDesc(orgId);
    }

    // == HELPER METHODS ==

    private int getPriceByType(String type) {
        if (type == null)
            return 244;

        switch (type) {
            case "BASIC":
                return 244;
            case "STANDARD":
                return 344;
            case "PREMIUM":
                return 544;
            default:
                return 244;
        }
    }

    private int getTotalDaysInCycle(Organization org) {
        return org.getBillingStartDate() == null ? 30 : org.getBillingStartDate().lengthOfMonth();
    }

    private int getRemainingDays(Organization org) {
        int days = (int) (org.getNextBillingDate().toEpochDay() - LocalDate.now().toEpochDay());
        return Math.max(days, 1);
    }
}
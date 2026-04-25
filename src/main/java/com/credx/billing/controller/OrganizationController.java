package com.credx.billing.controller;

import com.credx.billing.dto.BillingResponse;
import com.credx.billing.model.Invoice;
import com.credx.billing.model.Organization;
import com.credx.billing.service.OrganizationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orgs")
public class OrganizationController {

    private final OrganizationService service;

    public OrganizationController(OrganizationService service) {
        this.service = service;
    }

    //  1. Create Organization
    @PostMapping
    public Organization createOrg(@RequestBody Organization request) {
        return service.createOrganization(request.getName());
    }

    //  2. Add Seat (with proration + payment simulation)
    @PostMapping("/{orgId}/seats")
    public String addSeat(
            @PathVariable Long orgId,
            @RequestParam String email,
            @RequestParam String type
    ) {
        service.addSeat(orgId, email, type);
        return "Seat added";
    }

    //  3. Remove Seat
    @DeleteMapping("/seats/{seatId}")
    public String removeSeat(@PathVariable Long seatId) {
        service.removeSeat(seatId);
        return "Seat removed";
    }

    //  4. Seat Count
    @GetMapping("/{orgId}/seats/count")
    public int getSeatCount(@PathVariable Long orgId) {
        return service.getSeatBreakdown(orgId)
                      .values()
                      .stream()
                      .mapToInt(Integer::intValue)
                      .sum();
    }

    //  5. Seat Breakdown 
    @GetMapping("/{orgId}/seats")
    public Map<String, Integer> getSeats(@PathVariable Long orgId) {
        return service.getSeatBreakdown(orgId);
    }

    //  6. Get Bill (preview)
    @GetMapping("/{orgId}/bill")
    public BillingResponse getBill(@PathVariable Long orgId) {
        return service.calculateBill(orgId);
    }

    //  7. Run Monthly Billing manually (for testing)
    @GetMapping("/{orgId}/bill/run")
    public BillingResponse runBilling(@PathVariable Long orgId) {
        return service.generateMonthlyBill(orgId);
    }

    //  8. Billing History 
    @GetMapping("/{orgId}/billing/history")
    public List<Invoice> getBillingHistory(@PathVariable Long orgId) {
        return service.getBillingHistory(orgId);
    }
}
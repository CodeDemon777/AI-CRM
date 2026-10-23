package com.aicrm.controller;

import com.aicrm.exception.ResourceNotFoundException;
import com.aicrm.model.Lead;
import com.aicrm.service.LeadService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leads")
public class LeadController {

    private final LeadService leadService;

    @Autowired
    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @GetMapping
    public ResponseEntity<List<Lead>> getAllLeads(
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(leadService.getAllLeads(customerId, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lead> getLeadById(@PathVariable String id) {
        Lead lead = leadService.getLeadById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id));
        return ResponseEntity.ok(lead);
    }

    @PostMapping
    public ResponseEntity<Lead> createLead(@Valid @RequestBody Lead lead) {
        Lead created = leadService.createLead(lead);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Lead> updateLead(
            @PathVariable String id,
            @RequestBody Lead lead) {
        return ResponseEntity.ok(leadService.updateLead(id, lead));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Lead> updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be empty.");
        }
        return ResponseEntity.ok(leadService.updateLeadStatus(id, status.trim()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteLead(@PathVariable String id) {
        boolean removed = leadService.deleteLead(id);
        if (!removed) {
            throw new ResourceNotFoundException("Lead not found with id: " + id);
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Lead deleted successfully."
        ));
    }
}

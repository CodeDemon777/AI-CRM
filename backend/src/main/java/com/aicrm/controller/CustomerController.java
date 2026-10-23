package com.aicrm.controller;

import com.aicrm.exception.ResourceNotFoundException;
import com.aicrm.model.Customer;
import com.aicrm.service.CustomerService;
import com.aicrm.service.LeadService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final LeadService leadService;

    @Autowired
    public CustomerController(CustomerService customerService, LeadService leadService) {
        this.customerService = customerService;
        this.leadService = leadService;
    }

    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(customerService.getAllCustomers(search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable String id) {
        Customer customer = customerService.getCustomerById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        return ResponseEntity.ok(customer);
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody Customer customer) {
        Customer created = customerService.createCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(
            @PathVariable String id,
            @Valid @RequestBody Customer customer) {
        return ResponseEntity.ok(customerService.updateCustomer(id, customer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCustomer(@PathVariable String id) {
        boolean removed = customerService.deleteCustomer(id);
        if (!removed) {
            throw new ResourceNotFoundException("Customer not found with id: " + id);
        }
        // Cascade delete associated leads
        leadService.deleteLeadsByCustomerId(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Customer and associated leads deleted successfully."
        ));
    }
}

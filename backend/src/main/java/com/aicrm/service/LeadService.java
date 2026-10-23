package com.aicrm.service;

import com.aicrm.exception.ResourceNotFoundException;
import com.aicrm.model.Customer;
import com.aicrm.model.Lead;
import com.aicrm.repository.LeadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LeadService {

    private static final Logger log = LoggerFactory.getLogger(LeadService.class);

    private final LeadRepository leadRepository;
    private final CustomerService customerService;
    private final MongoHealthService mongoHealthService;

    // In-memory fallback map initialized with demo data
    private final Map<String, Lead> fallbackStore = new ConcurrentHashMap<>();

    @Autowired
    public LeadService(LeadRepository leadRepository, CustomerService customerService, MongoHealthService mongoHealthService) {
        this.leadRepository = leadRepository;
        this.customerService = customerService;
        this.mongoHealthService = mongoHealthService;
        initFallbackData();
    }

    private void initFallbackData() {
        Lead l1 = new Lead("101", "1", "Website Development", 25000.0, "New");
        l1.setCustomerName("Priya Sharma");
        Lead l2 = new Lead("102", "2", "CRM Subscription", 15000.0, "Contacted");
        l2.setCustomerName("Arun Kumar");
        fallbackStore.put(l1.getId(), l1);
        fallbackStore.put(l2.getId(), l2);
    }

    public List<Lead> getAllLeads(String customerId, String status) {
        if (mongoHealthService.isConnected()) {
            try {
                List<Lead> leads;
                if (customerId != null && !customerId.trim().isEmpty() && status != null && !status.trim().isEmpty()) {
                    leads = leadRepository.findByCustomerIdAndStatus(customerId.trim(), status.trim());
                } else if (customerId != null && !customerId.trim().isEmpty()) {
                    leads = leadRepository.findByCustomerId(customerId.trim());
                } else if (status != null && !status.trim().isEmpty()) {
                    leads = leadRepository.findByStatus(status.trim());
                } else {
                    leads = leadRepository.findAll();
                }

                // Populate customer names if needed
                for (Lead l : leads) {
                    if (l.getCustomerName() == null || l.getCustomerName().isEmpty()) {
                        customerService.getCustomerById(l.getCustomerId())
                                .ifPresent(c -> l.setCustomerName(c.getName()));
                    }
                }
                return leads;
            } catch (Exception e) {
                log.warn("Failed fetching leads from MongoDB: {}", e.getMessage());
            }
        }

        // Fallback store
        List<Lead> list = new ArrayList<>(fallbackStore.values());
        return list.stream()
                .filter(l -> customerId == null || customerId.trim().isEmpty() || customerId.equals(l.getCustomerId()))
                .filter(l -> status == null || status.trim().isEmpty() || status.equalsIgnoreCase(l.getStatus()))
                .peek(l -> {
                    if (l.getCustomerName() == null) {
                        customerService.getCustomerById(l.getCustomerId())
                                .ifPresent(c -> l.setCustomerName(c.getName()));
                    }
                })
                .toList();
    }

    public Optional<Lead> getLeadById(String id) {
        if (mongoHealthService.isConnected()) {
            try {
                return leadRepository.findById(id);
            } catch (Exception e) {
                log.warn("Failed fetching lead by id {}: {}", id, e.getMessage());
            }
        }
        return Optional.ofNullable(fallbackStore.get(id));
    }

    public Lead createLead(Lead lead) {
        if (lead.getCustomerId() == null || lead.getCustomerId().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID is required.");
        }

        // Resolve customer
        Customer customer = customerService.getCustomerById(lead.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + lead.getCustomerId()));

        lead.setCustomerName(customer.getName());
        lead.setTitle(lead.getTitle().trim());
        if (lead.getStatus() == null || lead.getStatus().trim().isEmpty()) {
            lead.setStatus("New");
        }
        lead.setCreatedAt(LocalDateTime.now());
        lead.setUpdatedAt(LocalDateTime.now());

        if (mongoHealthService.isConnected()) {
            try {
                return leadRepository.save(lead);
            } catch (Exception e) {
                log.warn("MongoDB lead save failed: {}", e.getMessage());
            }
        }

        if (lead.getId() == null || lead.getId().trim().isEmpty()) {
            lead.setId(String.valueOf(System.currentTimeMillis()));
        }

        fallbackStore.put(lead.getId(), lead);
        return lead;
    }

    public Lead updateLead(String id, Lead updated) {
        Lead existing = getLeadById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id));

        if (updated.getTitle() != null && !updated.getTitle().trim().isEmpty()) {
            existing.setTitle(updated.getTitle().trim());
        }
        if (updated.getValue() != null && updated.getValue() >= 0) {
            existing.setValue(updated.getValue());
        }
        if (updated.getStatus() != null && !updated.getStatus().trim().isEmpty()) {
            existing.setStatus(updated.getStatus().trim());
        }
        if (updated.getNotes() != null) {
            existing.setNotes(updated.getNotes());
        }
        existing.setUpdatedAt(LocalDateTime.now());

        if (mongoHealthService.isConnected()) {
            try {
                return leadRepository.save(existing);
            } catch (Exception e) {
                log.warn("MongoDB lead update failed: {}", e.getMessage());
            }
        }

        fallbackStore.put(id, existing);
        return existing;
    }

    public Lead updateLeadStatus(String id, String status) {
        Lead existing = getLeadById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id));

        existing.setStatus(status);
        existing.setUpdatedAt(LocalDateTime.now());

        if (mongoHealthService.isConnected()) {
            try {
                return leadRepository.save(existing);
            } catch (Exception e) {
                log.warn("MongoDB lead status update failed: {}", e.getMessage());
            }
        }

        fallbackStore.put(id, existing);
        return existing;
    }

    public boolean deleteLead(String id) {
        boolean removed = false;
        if (mongoHealthService.isConnected()) {
            try {
                if (leadRepository.existsById(id)) {
                    leadRepository.deleteById(id);
                    removed = true;
                }
            } catch (Exception e) {
                log.warn("MongoDB lead delete failed: {}", e.getMessage());
            }
        }

        if (fallbackStore.remove(id) != null) {
            removed = true;
        }

        return removed;
    }

    public void deleteLeadsByCustomerId(String customerId) {
        if (mongoHealthService.isConnected()) {
            try {
                leadRepository.deleteByCustomerId(customerId);
            } catch (Exception e) {
                log.warn("MongoDB cascade delete failed for customer {}: {}", customerId, e.getMessage());
            }
        }

        fallbackStore.values().removeIf(l -> customerId.equals(l.getCustomerId()));
    }
}

package com.aicrm.service;

import com.aicrm.exception.DuplicateResourceException;
import com.aicrm.exception.ResourceNotFoundException;
import com.aicrm.model.Customer;
import com.aicrm.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final MongoHealthService mongoHealthService;

    // In-memory fallback map initialized with demo data until MongoDB Atlas URI is supplied
    private final Map<String, Customer> fallbackStore = new ConcurrentHashMap<>();

    @Autowired
    public CustomerService(CustomerRepository customerRepository, MongoHealthService mongoHealthService) {
        this.customerRepository = customerRepository;
        this.mongoHealthService = mongoHealthService;
        initFallbackData();
    }

    private void initFallbackData() {
        Customer c1 = new Customer("1", "Priya Sharma", "priya@example.com", "Bright Solutions", "9876543210");
        Customer c2 = new Customer("2", "Arun Kumar", "arun@example.com", "TechNova", "9876501234");
        fallbackStore.put(c1.getId(), c1);
        fallbackStore.put(c2.getId(), c2);
    }

    public List<Customer> getAllCustomers(String search) {
        if (mongoHealthService.isConnected()) {
            try {
                if (search != null && !search.trim().isEmpty()) {
                    String query = search.trim();
                    return customerRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrCompanyContainingIgnoreCase(
                            query, query, query
                    );
                }
                return customerRepository.findAll();
            } catch (Exception e) {
                log.warn("Failed fetching from MongoDB, using fallback: {}", e.getMessage());
            }
        }

        // Fallback store
        List<Customer> list = new ArrayList<>(fallbackStore.values());
        if (search != null && !search.trim().isEmpty()) {
            String lower = search.trim().toLowerCase();
            return list.stream()
                    .filter(c -> (c.getName() != null && c.getName().toLowerCase().contains(lower)) ||
                                 (c.getEmail() != null && c.getEmail().toLowerCase().contains(lower)) ||
                                 (c.getCompany() != null && c.getCompany().toLowerCase().contains(lower)))
                    .toList();
        }
        return list;
    }

    public Optional<Customer> getCustomerById(String id) {
        if (mongoHealthService.isConnected()) {
            try {
                return customerRepository.findById(id);
            } catch (Exception e) {
                log.warn("MongoDB lookup failed for id {}: {}", id, e.getMessage());
            }
        }
        return Optional.ofNullable(fallbackStore.get(id));
    }

    public Customer createCustomer(Customer customer) {
        if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer email cannot be empty.");
        }

        String emailLower = customer.getEmail().trim().toLowerCase();
        customer.setEmail(emailLower);
        customer.setName(customer.getName().trim());
        customer.setCompany(customer.getCompany().trim());
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());

        if (mongoHealthService.isConnected()) {
            try {
                if (customerRepository.existsByEmail(emailLower)) {
                    throw new DuplicateResourceException("A customer with email " + emailLower + " already exists.");
                }
                return customerRepository.save(customer);
            } catch (DuplicateResourceException dre) {
                throw dre;
            } catch (Exception e) {
                log.warn("MongoDB save failed, persisting to fallback store: {}", e.getMessage());
            }
        }

        // Fallback duplicate check
        boolean exists = fallbackStore.values().stream()
                .anyMatch(c -> c.getEmail().equalsIgnoreCase(emailLower));
        if (exists) {
            throw new DuplicateResourceException("A customer with email " + emailLower + " already exists.");
        }

        if (customer.getId() == null || customer.getId().trim().isEmpty()) {
            customer.setId(String.valueOf(System.currentTimeMillis()));
        }

        fallbackStore.put(customer.getId(), customer);
        return customer;
    }

    public Customer updateCustomer(String id, Customer updated) {
        Customer existing = getCustomerById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        existing.setName(updated.getName().trim());
        existing.setCompany(updated.getCompany().trim());
        existing.setPhone(updated.getPhone() != null ? updated.getPhone().trim() : null);
        existing.setUpdatedAt(LocalDateTime.now());

        if (mongoHealthService.isConnected()) {
            try {
                return customerRepository.save(existing);
            } catch (Exception e) {
                log.warn("MongoDB update failed: {}", e.getMessage());
            }
        }

        fallbackStore.put(id, existing);
        return existing;
    }

    public boolean deleteCustomer(String id) {
        boolean removed = false;
        if (mongoHealthService.isConnected()) {
            try {
                if (customerRepository.existsById(id)) {
                    customerRepository.deleteById(id);
                    removed = true;
                }
            } catch (Exception e) {
                log.warn("MongoDB delete failed: {}", e.getMessage());
            }
        }

        if (fallbackStore.remove(id) != null) {
            removed = true;
        }

        return removed;
    }
}

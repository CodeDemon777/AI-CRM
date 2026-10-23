package com.aicrm.service;

import com.aicrm.model.Customer;
import com.aicrm.model.DashboardStats;
import com.aicrm.model.Lead;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final CustomerService customerService;
    private final LeadService leadService;
    private final MongoHealthService mongoHealthService;

    @Autowired
    public DashboardService(CustomerService customerService, LeadService leadService, MongoHealthService mongoHealthService) {
        this.customerService = customerService;
        this.leadService = leadService;
        this.mongoHealthService = mongoHealthService;
    }

    public DashboardStats getStats() {
        List<Customer> customers = customerService.getAllCustomers(null);
        List<Lead> leads = leadService.getAllLeads(null, null);

        long totalCustomers = customers.size();
        long totalLeads = leads.size();
        long convertedLeads = leads.stream().filter(l -> "Converted".equalsIgnoreCase(l.getStatus())).count();

        int conversionRate = totalLeads == 0 ? 0 : (int) Math.round(((double) convertedLeads / totalLeads) * 100);

        double totalPipelineValue = leads.stream()
                .mapToDouble(l -> l.getValue() != null ? l.getValue() : 0.0)
                .sum();

        Map<String, Long> statusCounts = new HashMap<>();
        statusCounts.put("New", leads.stream().filter(l -> "New".equalsIgnoreCase(l.getStatus())).count());
        statusCounts.put("Contacted", leads.stream().filter(l -> "Contacted".equalsIgnoreCase(l.getStatus())).count());
        statusCounts.put("Converted", convertedLeads);

        String dbStatus = mongoHealthService.getStatusDescription();

        return new DashboardStats(
                totalCustomers,
                totalLeads,
                convertedLeads,
                conversionRate,
                totalPipelineValue,
                statusCounts,
                dbStatus
        );
    }
}

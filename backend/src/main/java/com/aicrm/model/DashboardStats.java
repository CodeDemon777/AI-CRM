package com.aicrm.model;

import java.util.Map;

/**
 * Aggregated analytics and stats for the CRM dashboard.
 */
public class DashboardStats {

    private long totalCustomers;
    private long totalLeads;
    private long totalProducts;
    private long convertedLeads;
    private int conversionRate;
    private double totalPipelineValue;
    Map<String, Long> statusCounts;
    private String databaseStatus;

    public DashboardStats() {}

    public DashboardStats(long totalCustomers, long totalLeads, long totalProducts, long convertedLeads,
                          int conversionRate, double totalPipelineValue,
                          Map<String, Long> statusCounts, String databaseStatus) {
        this.totalCustomers = totalCustomers;
        this.totalLeads = totalLeads;
        this.totalProducts = totalProducts;
        this.convertedLeads = convertedLeads;
        this.conversionRate = conversionRate;
        this.totalPipelineValue = totalPipelineValue;
        this.statusCounts = statusCounts;
        this.databaseStatus = databaseStatus;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public long getTotalLeads() {
        return totalLeads;
    }

    public void setTotalLeads(long totalLeads) {
        this.totalLeads = totalLeads;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getConvertedLeads() {
        return convertedLeads;
    }

    public void setConvertedLeads(long convertedLeads) {
        this.convertedLeads = convertedLeads;
    }

    public int getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(int conversionRate) {
        this.conversionRate = conversionRate;
    }

    public double getTotalPipelineValue() {
        return totalPipelineValue;
    }

    public void setTotalPipelineValue(double totalPipelineValue) {
        this.totalPipelineValue = totalPipelineValue;
    }

    public Map<String, Long> getStatusCounts() {
        return statusCounts;
    }

    public void setStatusCounts(Map<String, Long> statusCounts) {
        this.statusCounts = statusCounts;
    }

    public String getDatabaseStatus() {
        return databaseStatus;
    }

    public void setDatabaseStatus(String databaseStatus) {
        this.databaseStatus = databaseStatus;
    }
}

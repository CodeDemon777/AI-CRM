package com.aicrm.model;

/**
 * Intelligent AI Insight representation for sales recommendations.
 */
public class AiInsight {

    private String leadId;
    private String customerId;
    private String customerName;
    private String opportunityTitle;
    private Double opportunityValue;
    private String status;
    private String title;
    private String description;
    private String action;
    private String priority; // HIGH, MEDIUM, LOW
    private int score; // 0 - 100 win probability or urgency score

    public AiInsight() {}

    public AiInsight(String leadId, String customerId, String customerName, String opportunityTitle,
                     Double opportunityValue, String status, String title, String description,
                     String action, String priority, int score) {
        this.leadId = leadId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.opportunityTitle = opportunityTitle;
        this.opportunityValue = opportunityValue;
        this.status = status;
        this.title = title;
        this.description = description;
        this.action = action;
        this.priority = priority;
        this.score = score;
    }

    public String getLeadId() {
        return leadId;
    }

    public void setLeadId(String leadId) {
        this.leadId = leadId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getOpportunityTitle() {
        return opportunityTitle;
    }

    public void setOpportunityTitle(String opportunityTitle) {
        this.opportunityTitle = opportunityTitle;
    }

    public Double getOpportunityValue() {
        return opportunityValue;
    }

    public void setOpportunityValue(Double opportunityValue) {
        this.opportunityValue = opportunityValue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}

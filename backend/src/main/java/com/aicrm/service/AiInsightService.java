package com.aicrm.service;

import com.aicrm.model.AiInsight;
import com.aicrm.model.Lead;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AiInsightService {

    private final LeadService leadService;

    @Autowired
    public AiInsightService(LeadService leadService) {
        this.leadService = leadService;
    }

    /**
     * Generate dynamic AI insights for all current leads.
     */
    public List<AiInsight> generateInsights() {
        List<Lead> leads = leadService.getAllLeads(null, null);
        List<AiInsight> insights = new ArrayList<>();

        for (Lead lead : leads) {
            insights.add(analyzeLead(lead));
        }

        // Sort by priority (HIGH first), then by score descending
        insights.sort(Comparator
                .comparing((AiInsight i) -> priorityWeight(i.getPriority()))
                .thenComparing(AiInsight::getScore).reversed());

        return insights;
    }

    /**
     * Intelligent recommendation engine for an individual sales opportunity.
     */
    public AiInsight analyzeLead(Lead lead) {
        String status = lead.getStatus() != null ? lead.getStatus() : "New";
        double value = lead.getValue() != null ? lead.getValue() : 0.0;

        String title;
        String description;
        String action;
        String priority;
        int score;

        switch (status) {
            case "New":
                if (value >= 50000) {
                    title = "High-Value Opportunity Alert";
                    description = String.format("Opportunity '%s' is valued at ₹%.0f. Expedite initial contact to capture maximum buyer intent.",
                            lead.getTitle(), value);
                    action = "Schedule Executive Discovery Call Today";
                    priority = "HIGH";
                    score = 85;
                } else {
                    title = "Initial Outreach Required";
                    description = String.format("Introduce your solution to %s and understand their project requirements.",
                            lead.getCustomerName() != null ? lead.getCustomerName() : "the prospect");
                    action = "Send Introduction Email & Product Overview";
                    priority = "MEDIUM";
                    score = 45;
                }
                break;

            case "Contacted":
                if (value >= 30000) {
                    title = "Proposal & Negotiation Phase";
                    description = "Lead has responded. Prepare a tailored quotation and address any technical or pricing inquiries.";
                    action = "Deliver Tailored Scope Proposal";
                    priority = "HIGH";
                    score = 75;
                } else {
                    title = "Follow-up Cadence";
                    description = "Maintain momentum. Check in on customer questions and schedule a demonstration.";
                    action = "Follow up within 48 hours";
                    priority = "MEDIUM";
                    score = 60;
                }
                break;

            case "Converted":
                title = "Client Onboarding & Retention";
                description = "Opportunity won! Kickstart onboarding, deliver initial milestones, and nurture for future expansion.";
                action = "Initiate Onboarding & Customer Success Checklist";
                priority = "LOW";
                score = 100;
                break;

            default:
                title = "Opportunity Review";
                description = "Review lead timeline and assess engagement opportunities.";
                action = "Assess qualification criteria";
                priority = "LOW";
                score = 30;
                break;
        }

        return new AiInsight(
                lead.getId(),
                lead.getCustomerId(),
                lead.getCustomerName(),
                lead.getTitle(),
                lead.getValue(),
                lead.getStatus(),
                title,
                description,
                action,
                priority,
                score
        );
    }

    private int priorityWeight(String priority) {
        if ("HIGH".equalsIgnoreCase(priority)) return 3;
        if ("MEDIUM".equalsIgnoreCase(priority)) return 2;
        return 1;
    }
}

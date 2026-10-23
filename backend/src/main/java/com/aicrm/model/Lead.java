package com.aicrm.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Sales Lead / Opportunity domain model mapped to MongoDB 'leads' collection.
 */
@Document(collection = "leads")
public class Lead {

    @Id
    private String id;

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    private String customerName;

    @NotBlank(message = "Opportunity title is required")
    private String title;

    @NotNull(message = "Opportunity value is required")
    @Min(value = 0, message = "Opportunity value must be zero or positive")
    private Double value;

    @NotBlank(message = "Lead status is required")
    private String status = "New"; // "New", "Contacted", "Converted", "Lost"

    private String notes;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Lead() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Lead(String id, String customerId, String title, Double value, String status) {
        this.id = id;
        this.customerId = customerId;
        this.title = title;
        this.value = value;
        this.status = status != null ? status : "New";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Lead{" +
                "id='" + id + '\'' +
                ", customerId='" + customerId + '\'' +
                ", title='" + title + '\'' +
                ", value=" + value +
                ", status='" + status + '\'' +
                '}';
    }
}

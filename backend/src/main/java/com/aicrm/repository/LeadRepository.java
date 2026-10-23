package com.aicrm.repository;

import com.aicrm.model.Lead;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadRepository extends MongoRepository<Lead, String> {

    List<Lead> findByCustomerId(String customerId);

    List<Lead> findByStatus(String status);

    List<Lead> findByCustomerIdAndStatus(String customerId, String status);

    void deleteByCustomerId(String customerId);
}

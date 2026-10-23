package com.aicrm.controller;

import com.aicrm.model.AiInsight;
import com.aicrm.model.Lead;
import com.aicrm.service.AiInsightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiInsightController {

    private final AiInsightService aiInsightService;

    @Autowired
    public AiInsightController(AiInsightService aiInsightService) {
        this.aiInsightService = aiInsightService;
    }

    @GetMapping("/insights")
    public ResponseEntity<List<AiInsight>> getInsights() {
        return ResponseEntity.ok(aiInsightService.generateInsights());
    }

    @PostMapping("/analyze")
    public ResponseEntity<AiInsight> analyzeCustomLead(@RequestBody Lead lead) {
        return ResponseEntity.ok(aiInsightService.analyzeLead(lead));
    }
}

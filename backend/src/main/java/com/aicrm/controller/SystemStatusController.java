package com.aicrm.controller;

import com.aicrm.service.MongoHealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SystemStatusController {

    private final MongoHealthService mongoHealthService;

    @Value("${spring.data.mongodb.uri:Not Configured}")
    private String mongoUri;

    @Autowired
    public SystemStatusController(MongoHealthService mongoHealthService) {
        this.mongoHealthService = mongoHealthService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean connected = mongoHealthService.isConnected();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("databaseConnected", connected);
        response.put("databaseMode", mongoHealthService.getStatusDescription());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/system/status")
    public ResponseEntity<Map<String, Object>> systemStatus() {
        boolean connected = mongoHealthService.isConnected();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("application", "AI-CRM Spring Boot Backend");
        response.put("version", "1.0.0");
        response.put("javaVersion", System.getProperty("java.version"));
        response.put("database", "MongoDB Atlas");
        response.put("databaseConnected", connected);
        response.put("currentDatabaseMode", mongoHealthService.getStatusDescription());

        // Mask URI if present
        String maskedUri = mongoUri;
        if (maskedUri != null && maskedUri.contains("@")) {
            int atIndex = maskedUri.indexOf("@");
            maskedUri = "mongodb+srv://****:****" + maskedUri.substring(atIndex);
        }
        response.put("configuredUri", maskedUri);

        if (!connected) {
            response.put("instructions", Map.of(
                    "note", "MongoDB Atlas connection URI can be provided whenever you are ready.",
                    "step1", "Open 'backend/src/main/resources/application.properties' or set the MONGODB_URI environment variable.",
                    "step2", "Replace 'spring.data.mongodb.uri' with your MongoDB Atlas connection string.",
                    "example", "mongodb+srv://<username>:<password>@cluster0.mongodb.net/ai_crm?retryWrites=true&w=majority"
            ));
        }

        return ResponseEntity.ok(response);
    }
}

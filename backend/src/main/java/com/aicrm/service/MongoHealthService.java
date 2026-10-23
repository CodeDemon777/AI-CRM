package com.aicrm.service;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class MongoHealthService {

    private static final Logger log = LoggerFactory.getLogger(MongoHealthService.class);

    private final MongoTemplate mongoTemplate;
    private Boolean cachedStatus = null;
    private long lastCheckedTime = 0;
    private static final long CACHE_TTL_MS = 10000; // recheck every 10 seconds

    public MongoHealthService() {
        this.mongoTemplate = null;
    }

    @Autowired
    public MongoHealthService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Checks if MongoDB Atlas (or local instance) is actively reachable.
     */
    public boolean isConnected() {
        long now = System.currentTimeMillis();
        if (cachedStatus != null && (now - lastCheckedTime) < CACHE_TTL_MS) {
            return cachedStatus;
        }

        if (mongoTemplate == null) {
            cachedStatus = false;
            lastCheckedTime = now;
            return false;
        }

        try {
            Document ping = mongoTemplate.executeCommand(new Document("ping", 1));
            boolean ok = ping != null && ping.get("ok") != null && Double.parseDouble(ping.get("ok").toString()) == 1.0;
            cachedStatus = ok;
            lastCheckedTime = now;
            return ok;
        } catch (Exception e) {
            log.warn("MongoDB connection check failed: {}. Operating in fallback mode.", e.getMessage());
            cachedStatus = false;
            lastCheckedTime = now;
            return false;
        }
    }

    public String getStatusDescription() {
        if (isConnected()) {
            return "MongoDB Atlas (Connected)";
        }
        return "Demo Fallback Mode (Awaiting MongoDB Atlas URI in application.properties)";
    }
}

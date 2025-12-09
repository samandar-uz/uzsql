package org.example.uzsql.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, LimitRecord> limits = new ConcurrentHashMap<>();

    public boolean allow(String ip) {
        LimitRecord record = limits.get(ip);

        long now = System.currentTimeMillis();

        if (record == null) {
            // birinchi marta request
            limits.put(ip, new LimitRecord(1, now));
            return true;
        }

        // 24 soat = 86400000 ms
        long diff = now - record.startTime();

        if (diff > 86400000) {
            // yangi kun boshlandi → reset
            limits.put(ip, new LimitRecord(1, now));
            return true;
        }

        // limit yetganmi?
        if (record.count() >= 5) {
            return false;
        }

        // yana request qo‘shamiz
        limits.put(ip, new LimitRecord(record.count() + 1, record.startTime()));
        return true;
    }

    private record LimitRecord(int count, long startTime) {}
}

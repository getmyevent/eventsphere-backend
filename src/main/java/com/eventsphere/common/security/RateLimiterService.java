package com.eventsphere.common.security;

import com.eventsphere.common.error.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private final int maxRequestsPerMinutePerIp;
    private final int maxRequestsPerMinutePerMobile;

    private final ConcurrentHashMap<String, Deque<Long>> ipRequestTimestamps = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Deque<Long>> mobileRequestTimestamps = new ConcurrentHashMap<>();

    public RateLimiterService(
            @Value("${eventsphere.security.rate-limit.ip-per-minute:600}") int maxRequestsPerMinutePerIp,
            @Value("${eventsphere.security.rate-limit.mobile-per-minute:10}") int maxRequestsPerMinutePerMobile
    ) {
        this.maxRequestsPerMinutePerIp = maxRequestsPerMinutePerIp;
        this.maxRequestsPerMinutePerMobile = maxRequestsPerMinutePerMobile;
    }

    public void checkRateLimit(String clientIp, String mobile) {
        long now = Instant.now().toEpochMilli();
        long oneMinuteAgo = now - 60_000L;

        // 1. Check IP rate limit
        if (clientIp != null && !clientIp.isBlank()) {
            Deque<Long> ipTimestamps = ipRequestTimestamps.computeIfAbsent(clientIp, k -> new ArrayDeque<>());
            synchronized (ipTimestamps) {
                while (!ipTimestamps.isEmpty() && ipTimestamps.peekFirst() < oneMinuteAgo) {
                    ipTimestamps.pollFirst();
                }
                if (ipTimestamps.size() >= maxRequestsPerMinutePerIp) {
                    throw new BusinessException(
                            "Too many requests from this IP. Please wait a minute before trying again.",
                            HttpStatus.TOO_MANY_REQUESTS
                    );
                }
                ipTimestamps.addLast(now);
            }
        }

        // 2. Check mobile number rate limit
        if (mobile != null && !mobile.isBlank()) {
            Deque<Long> mobileTimestamps = mobileRequestTimestamps.computeIfAbsent(mobile, k -> new ArrayDeque<>());
            synchronized (mobileTimestamps) {
                while (!mobileTimestamps.isEmpty() && mobileTimestamps.peekFirst() < oneMinuteAgo) {
                    mobileTimestamps.pollFirst();
                }
                if (mobileTimestamps.size() >= maxRequestsPerMinutePerMobile) {
                    throw new BusinessException(
                            "Too many access attempts for this mobile number. Please wait a minute before trying again.",
                            HttpStatus.TOO_MANY_REQUESTS
                    );
                }
                mobileTimestamps.addLast(now);
            }
        }
    }

    // Periodic purge of empty/stale entries every 5 minutes to prevent memory leak
    @Scheduled(fixedRate = 300_000L)
    public void cleanupStaleEntries() {
        long fiveMinutesAgo = Instant.now().toEpochMilli() - 300_000L;

        ipRequestTimestamps.entrySet().removeIf(entry -> {
            Deque<Long> deque = entry.getValue();
            synchronized (deque) {
                while (!deque.isEmpty() && deque.peekFirst() < fiveMinutesAgo) {
                    deque.pollFirst();
                }
                return deque.isEmpty();
            }
        });

        mobileRequestTimestamps.entrySet().removeIf(entry -> {
            Deque<Long> deque = entry.getValue();
            synchronized (deque) {
                while (!deque.isEmpty() && deque.peekFirst() < fiveMinutesAgo) {
                    deque.pollFirst();
                }
                return deque.isEmpty();
            }
        });
    }

    // Test helper to reset limiter state
    public void reset() {
        ipRequestTimestamps.clear();
        mobileRequestTimestamps.clear();
    }
}

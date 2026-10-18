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
import java.util.concurrent.locks.ReentrantLock;

@Service
public class RateLimiterService {

    private final int maxRequestsPerMinutePerIp;
    private final int maxRequestsPerMinutePerMobile;

    private final ConcurrentHashMap<String, SlidingWindowBucket> ipBuckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, SlidingWindowBucket> mobileBuckets = new ConcurrentHashMap<>();

    public RateLimiterService(
            @Value("${eventsphere.security.rate-limit.ip-per-minute:600}") int maxRequestsPerMinutePerIp,
            @Value("${eventsphere.security.rate-limit.mobile-per-minute:10}") int maxRequestsPerMinutePerMobile
    ) {
        this.maxRequestsPerMinutePerIp = maxRequestsPerMinutePerIp;
        this.maxRequestsPerMinutePerMobile = maxRequestsPerMinutePerMobile;
    }

    public void checkRateLimit(String clientIp, String mobile) {
        long now = Instant.now().toEpochMilli();
        long oneMinuteMs = 60_000L;

        // 1. Check IP rate limit (Virtual-thread safe via ReentrantLock)
        if (clientIp != null && !clientIp.isBlank()) {
            SlidingWindowBucket ipBucket = ipBuckets.computeIfAbsent(clientIp, k -> new SlidingWindowBucket());
            if (!ipBucket.tryAcquire(now, oneMinuteMs, maxRequestsPerMinutePerIp)) {
                throw new BusinessException(
                        "Too many requests from this IP. Please wait a minute before trying again.",
                        HttpStatus.TOO_MANY_REQUESTS
                );
            }
        }

        // 2. Check mobile number rate limit (Virtual-thread safe via ReentrantLock)
        if (mobile != null && !mobile.isBlank()) {
            SlidingWindowBucket mobileBucket = mobileBuckets.computeIfAbsent(mobile, k -> new SlidingWindowBucket());
            if (!mobileBucket.tryAcquire(now, oneMinuteMs, maxRequestsPerMinutePerMobile)) {
                throw new BusinessException(
                        "Too many access attempts for this mobile number. Please wait a minute before trying again.",
                        HttpStatus.TOO_MANY_REQUESTS
                );
            }
        }
    }

    // Periodic purge of empty/stale entries every 5 minutes to prevent memory leak
    @Scheduled(fixedRate = 300_000L)
    public void cleanupStaleEntries() {
        long fiveMinutesAgo = Instant.now().toEpochMilli() - 300_000L;
        ipBuckets.entrySet().removeIf(entry -> entry.getValue().isStale(fiveMinutesAgo));
        mobileBuckets.entrySet().removeIf(entry -> entry.getValue().isStale(fiveMinutesAgo));
    }

    // Test helper to reset limiter state
    public void reset() {
        ipBuckets.clear();
        mobileBuckets.clear();
    }

    /**
     * Sliding window bucket utilizing ReentrantLock.
     * ReentrantLock integrates with Java 21 Virtual Threads (Loom),
     * allowing carrier threads to yield without carrier thread pinning.
     */
    private static class SlidingWindowBucket {
        private final Deque<Long> timestamps = new ArrayDeque<>();
        private final ReentrantLock lock = new ReentrantLock();

        public boolean tryAcquire(long now, long windowMs, int maxRequests) {
            long threshold = now - windowMs;
            lock.lock();
            try {
                while (!timestamps.isEmpty() && timestamps.peekFirst() < threshold) {
                    timestamps.pollFirst();
                }
                if (timestamps.size() >= maxRequests) {
                    return false;
                }
                timestamps.addLast(now);
                return true;
            } finally {
                lock.unlock();
            }
        }

        public boolean isStale(long threshold) {
            lock.lock();
            try {
                while (!timestamps.isEmpty() && timestamps.peekFirst() < threshold) {
                    timestamps.pollFirst();
                }
                return timestamps.isEmpty();
            } finally {
                lock.unlock();
            }
        }
    }
}

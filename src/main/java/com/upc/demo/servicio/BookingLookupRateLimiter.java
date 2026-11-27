package com.upc.demo.servicio;

import com.upc.demo.config.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory sliding window rate limiter for public booking lookup and voucher resend endpoints.
 * Protects against brute-force attacks and resource exhaustion.
 */
@Component
public class BookingLookupRateLimiter {

    static final int MAX_REQUESTS_PER_WINDOW = 15;
    static final long WINDOW_DURATION_SECONDS = 60;
    private static final int MAX_ENTRIES_BEFORE_CLEANUP = 1000;

    private final ConcurrentHashMap<String, RequestWindow> tracker = new ConcurrentHashMap<>();

    public void checkRateLimit(HttpServletRequest request) {
        String clientIp = resolveClientIp(request);
        checkRateLimitForIp(clientIp);
    }

    public void checkRateLimitForIp(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            return;
        }

        long now = Instant.now().getEpochSecond();

        if (tracker.size() > MAX_ENTRIES_BEFORE_CLEANUP) {
            cleanupExpired(now);
        }

        RequestWindow window = tracker.compute(clientIp, (key, existing) -> {
            if (existing == null || (now - existing.windowStart) >= WINDOW_DURATION_SECONDS) {
                return new RequestWindow(now, new AtomicInteger(1));
            }
            existing.count.incrementAndGet();
            return existing;
        });

        if (window.count.get() > MAX_REQUESTS_PER_WINDOW) {
            throw new RateLimitExceededException("Demasiadas consultas de reservas. Por favor, espere un minuto antes de intentar nuevamente.");
        }
    }

    public void reset() {
        tracker.clear();
    }

    private void cleanupExpired(long now) {
        tracker.entrySet().removeIf(entry -> (now - entry.getValue().windowStart) >= WINDOW_DURATION_SECONDS);
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private static class RequestWindow {
        final long windowStart;
        final AtomicInteger count;

        RequestWindow(long windowStart, AtomicInteger count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}

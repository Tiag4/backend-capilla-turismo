package com.upc.demo.servicio;

import com.upc.demo.config.exception.RateLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingLookupRateLimiterTest {

    private BookingLookupRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new BookingLookupRateLimiter();
    }

    @Test
    @DisplayName("rateLimiter: permite hasta el máximo de solicitudes configuradas dentro de la ventana")
    void allowsRequestsWithinLimit() {
        String testIp = "192.168.1.100";
        for (int i = 0; i < BookingLookupRateLimiter.MAX_REQUESTS_PER_WINDOW; i++) {
            assertThatCode(() -> rateLimiter.checkRateLimitForIp(testIp))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    @DisplayName("rateLimiter: lanza RateLimitExceededException al superar el límite de solicitudes")
    void throwsRateLimitExceededExceptionWhenLimitExceeded() {
        String testIp = "192.168.1.101";
        for (int i = 0; i < BookingLookupRateLimiter.MAX_REQUESTS_PER_WINDOW; i++) {
            rateLimiter.checkRateLimitForIp(testIp);
        }

        assertThatThrownBy(() -> rateLimiter.checkRateLimitForIp(testIp))
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessageContaining("Demasiadas consultas de reservas");
    }

    @Test
    @DisplayName("rateLimiter: aísla límites entre diferentes direcciones IP")
    void isolatesLimitsBetweenDifferentIps() {
        String ipA = "10.0.0.1";
        String ipB = "10.0.0.2";

        for (int i = 0; i < BookingLookupRateLimiter.MAX_REQUESTS_PER_WINDOW; i++) {
            rateLimiter.checkRateLimitForIp(ipA);
        }

        // IP A is at limit, IP B should still be allowed
        assertThatThrownBy(() -> rateLimiter.checkRateLimitForIp(ipA))
                .isInstanceOf(RateLimitExceededException.class);

        assertThatCode(() -> rateLimiter.checkRateLimitForIp(ipB))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("rateLimiter: ignora IPs nulas o vacías sin arrojar excepción")
    void ignoresNullOrBlankIp() {
        assertThatCode(() -> rateLimiter.checkRateLimitForIp(null))
                .doesNotThrowAnyException();
        assertThatCode(() -> rateLimiter.checkRateLimitForIp("   "))
                .doesNotThrowAnyException();
    }
}

package com.assesment.ragchat.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import io.github.bucket4j.redis.jedis.cas.JedisBasedProxyManager;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import redis.clients.jedis.JedisPool;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final JedisBasedProxyManager proxyManager;

    private static final String[] RATE_LIMIT_BYPASS_PATHS = {
            "/v3/api-docs",
            "/swagger-ui",
            "/actuator/health",
            "/api/data"
    };

    public RateLimitingFilter(JedisPool jedisPool) {
        this.proxyManager = JedisBasedProxyManager.builderFor(jedisPool)
                .withExpirationStrategy(ExpirationAfterWriteStrategy.fixedTimeToLive(Duration.ofMinutes(10)))
                .build();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestURI = request.getRequestURI();

        for (String path : RATE_LIMIT_BYPASS_PATHS) {
            if (requestURI.startsWith(path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String userKey = extractKey(request);
        byte[] key = userKey.getBytes(StandardCharsets.UTF_8);

        Bucket bucket = proxyManager.builder().build(key, () ->
                BucketConfiguration.builder()
                        .addLimit(Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1))))
                        .build());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Rate limit exceeded");
        }
    }

    private String extractKey(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}

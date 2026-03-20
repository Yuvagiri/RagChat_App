package com.assesment.ragchat.controller;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.redis.jedis.cas.JedisBasedProxyManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@RestController
public class RateLimitedController {

    private final JedisBasedProxyManager proxyManager;

    public RateLimitedController(JedisBasedProxyManager proxyManager) {
        this.proxyManager = proxyManager;
    }

    @GetMapping("/api/data")
    public ResponseEntity<String> getData(@RequestHeader("X-User-Id") String userId) {

        System.out.println("\n\nRequest from user: " + userId);
        // Convert userId String to byte[] key for Redis
        byte[] keyBytes = userId.getBytes(StandardCharsets.UTF_8);

        // Build or get bucket associated with this key
        Bucket bucket = proxyManager.builder().build(keyBytes, () ->
                BucketConfiguration.builder()
                        .addLimit(Bandwidth.classic(
                                5, // capacity: 5 tokens
                                Refill.greedy(5, Duration.ofMinutes(1)) // refill 5 tokens per 1 minute
                        ))
                        .build());

        long tokensLeft = bucket.getAvailableTokens();
        System.out.println("Remaining tokens for user " + userId + ": " + tokensLeft);
        // Try to consume 1 token for the request
        if (bucket.tryConsume(1)) {
            // Allowed
            return ResponseEntity.ok("Here is your data!");
        } else {
            // Rate limit exceeded
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Rate limit exceeded");
        }
    }
}

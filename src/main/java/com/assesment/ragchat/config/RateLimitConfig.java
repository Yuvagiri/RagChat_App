package com.assesment.ragchat.config;

import io.github.bucket4j.redis.jedis.cas.JedisBasedProxyManager;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.rate-limit.sessions.expiry-minutes:60}")
    private int timeForRefillingBucketUpToMax;

    @Bean
    public JedisPool jedisPool() {
        JedisPool pool = new JedisPool(redisHost, redisPort);

        // Optional: test the connection during startup
        try (Jedis jedis = pool.getResource()) {
            String response = jedis.ping();
            if (!"PONG".equalsIgnoreCase(response)) {
                throw new IllegalStateException("Failed to connect to Redis: PING returned " + response);
            }
            System.out.println("✅ Connected to Redis successfully");
        } catch (Exception e) {
            System.err.println("❌ Redis connection failed: " + e.getMessage());
            // Optionally: rethrow to fail startup
            throw new RuntimeException("Redis connection failed", e);
        }

        return pool;
    }

    @Bean
    public JedisBasedProxyManager proxyManager(JedisPool jedisPool) {
        // Use ExpirationAfterWriteStrategy from distributed package
        ExpirationAfterWriteStrategy expiration = ExpirationAfterWriteStrategy
                .basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(timeForRefillingBucketUpToMax));

        return JedisBasedProxyManager
                .builderFor(jedisPool)
                .withExpirationStrategy(expiration)
                .build();
    }
}
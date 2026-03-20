package com.assesment.ragchat.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Configuration
    @ConfigurationProperties(prefix = "security")
    public static class ApiKeyConfiguration {

        private List<ApiKeyConfig> apiKeys;

        public List<ApiKeyConfig> getApiKeys() {
            return apiKeys;
        }

        public void setApiKeys(List<ApiKeyConfig> apiKeys) {
            this.apiKeys = apiKeys;
        }
    }

    public static class ApiKeyConfig {

        private String key;
        private String role;

        public ApiKeyConfig() {
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    private final ApiKeyConfiguration apiKeyConfiguration;

    public SecurityConfig(ApiKeyConfiguration apiKeyConfiguration) {
        this.apiKeyConfiguration = apiKeyConfiguration;
    }

    @Bean
    public ApiKeyFilter apiKeyFilter() {

        List<ApiKeyConfig> apiKeyConfigs = apiKeyConfiguration.getApiKeys();
        Map<String, String> keyToRoleMap = apiKeyConfigs.stream()
                .collect(Collectors.toMap(ApiKeyConfig::getKey, ApiKeyConfig::getRole));

        return new ApiKeyFilter(keyToRoleMap);
    }

    private static final String[] PUBLIC_WHITELIST = {
            // Swagger/OpenAPI Endpoints
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",

            // Other explicitly public endpoints
            "/actuator/health",
            "/api/data"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(apiKeyFilter(), BasicAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_WHITELIST).permitAll()
                        .requestMatchers("/rag_chat_storage/**").hasAnyRole("ADMIN", "SESSIONS_USER")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().denyAll()
                )

                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .cors(withDefaults());

        return http.build();
    }

    private static class ApiKeyFilter extends OncePerRequestFilter {

        private final Map<String, String> keyToRoleMap;
        private final String headerName = "X-Api-Key";

        public ApiKeyFilter(Map<String, String> keyToRoleMap) {
            this.keyToRoleMap = keyToRoleMap;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {

            String apiKey = request.getHeader(headerName);

            if (apiKey != null) {
                String requiredRole = keyToRoleMap.get(apiKey);

                if (requiredRole != null) {
                    log.debug("API Key found. Authenticating as role: ROLE_{}", requiredRole);
                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            "API_USER_" + requiredRole,
                            null,
                            AuthorityUtils.createAuthorityList("ROLE_" + requiredRole)
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    log.warn("Invalid API Key submitted.");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Invalid or missing API Key in the X-Api-Key header.\"}");
                    return;
                }
            }

            filterChain.doFilter(request, response);
        }
    }

   /* // Production-level CORS configuration (Uncomment and configure if needed)
    // @Bean
    // public CorsConfigurationSource corsConfigurationSource() {
    //
    //     CorsConfiguration configuration = new CorsConfiguration();
    //     configuration.setAllowedOrigins(List.of("https://your-production-frontend.com"));
    //     configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    //     configuration.setAllowedHeaders(List.of("*"));
    //     configuration.setAllowCredentials(true);
    //     UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    //     source.registerCorsConfiguration("/**", configuration);
    //     return source;
    // }*/
}
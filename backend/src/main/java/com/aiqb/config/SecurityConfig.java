package com.aiqb.config;

import com.aiqb.common.Result;
import com.aiqb.security.JwtAuthFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Spring Security 配置
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CorsFilter corsFilter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 仅在 dev profile 下暴露开发期密码重置接口（/dev/**）。
     * 任何其他 profile（prod/test 等）下都不会把 /dev/** 加进白名单，
     * 防止 DevPasswordResetController 被未鉴权访问。
     */
    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        List<String> permitAllPaths = new ArrayList<>();
        permitAllPaths.add("/auth/login");
        permitAllPaths.add("/auth/register");
        // 开发期密码重置工具：仅 dev profile 开放
        if ("dev".equalsIgnoreCase(activeProfile)) {
            permitAllPaths.add("/dev/**");
        }
        // Knife4j / Swagger 文档相关
        permitAllPaths.add("/doc.html");
        permitAllPaths.add("/swagger-ui.html");
        permitAllPaths.add("/swagger-ui/**");
        permitAllPaths.add("/swagger-resources/**");
        permitAllPaths.add("/swagger-resources/configuration/**");
        permitAllPaths.add("/v3/api-docs/**");
        permitAllPaths.add("/v3/api-docs/swagger-config");
        permitAllPaths.add("/webjars/**");
        permitAllPaths.add("/favicon.ico");
        permitAllPaths.add("/error");

        String[] permitAll = permitAllPaths.toArray(new String[0]);

        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(permitAll).permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .exceptionHandling(eh -> eh
                .authenticationEntryPoint((req, resp, ex) -> {
                    resp.setStatus(401);
                    resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    resp.getWriter().write(objectMapper.writeValueAsString(Result.unauthorized("请先登录")));
                })
                .accessDeniedHandler((req, resp, ex) -> {
                    resp.setStatus(403);
                    resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    resp.getWriter().write(objectMapper.writeValueAsString(Result.forbidden("权限不足")));
                })
            )
            .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

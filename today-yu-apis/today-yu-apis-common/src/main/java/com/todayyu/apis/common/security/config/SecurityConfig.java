package com.todayyu.apis.common.security.config;

import com.todayyu.apis.common.security.filter.JwtAuthenticationFilter;
import com.todayyu.apis.common.security.handler.SecurityErrorResponseWriter;
import com.todayyu.application.auth.usecase.AuthenticationUsecase;
import com.todayyu.domain.member.vo.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final AccessDeniedHandler accessDeniedHandler;
    private final AuthenticationUsecase authenticationUsecase;
    private final SecurityErrorResponseWriter securityErrorResponseWriter;
    
    private static final RequestMatcher PUBLIC_ENDPOINTS = new OrRequestMatcher(
            PathPatternRequestMatcher.pathPattern("/api/v1/auth/sign-in"),
            PathPatternRequestMatcher.pathPattern("/api/v1/auth/sign-up"),
            PathPatternRequestMatcher.pathPattern("/api/v1/auth/email-verification/**"),
            PathPatternRequestMatcher.pathPattern("/api/v1/auth/reissue"),
            PathPatternRequestMatcher.pathPattern("/api/v1/auth/password-reset-verification/**")
    );
    
    private static final RequestMatcher MEMBER_ENDPOINTS = new OrRequestMatcher(
            PathPatternRequestMatcher.pathPattern("/api/v1/auth/sign-out"),
            PathPatternRequestMatcher.pathPattern("/api/v1/infos/**"),
            PathPatternRequestMatcher.pathPattern("/api/v1/notices/**"),
            PathPatternRequestMatcher.pathPattern("/api/v1/inquiries/**"),
            PathPatternRequestMatcher.pathPattern("/api/v1/members/**")
    );
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session
                        -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize
                        -> authorize
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(MEMBER_ENDPOINTS).hasRole(Role.MEMBER.name())
                        .anyRequest().authenticated())
                .exceptionHandling(exception
                        -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(
                        new JwtAuthenticationFilter(authenticationUsecase, securityErrorResponseWriter, PUBLIC_ENDPOINTS),
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }
}

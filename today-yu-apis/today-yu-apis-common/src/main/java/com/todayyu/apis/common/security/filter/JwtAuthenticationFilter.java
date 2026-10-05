package com.todayyu.apis.common.security.filter;

import com.todayyu.apis.common.auth.AuthScheme;
import com.todayyu.apis.common.security.handler.CustomAuthenticationEntryPoint;
import com.todayyu.apis.common.security.handler.SecurityErrorResponseWriter;
import com.todayyu.apis.common.security.principal.CustomUserDetails;
import com.todayyu.application.auth.dto.AuthenticatedMember;
import com.todayyu.application.auth.error.BusinessAuthErrorType;
import com.todayyu.application.auth.usecase.AuthenticationUsecase;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.auth.vo.AccessToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final AuthenticationUsecase authenticationUsecase;
    private final SecurityErrorResponseWriter responseWriter;
    
    private static final String AUTHORIZATION_PREFIX = AuthScheme.BEARER + " ";
    
    /**
     * JWT 토큰을 통한 인증 정보 등록 메서드
     *
     * <p> 사용자 HTTP 요청의 {@code Authorization} 헤더에 담긴 토큰을 파싱하여 인증 정보를 등록한다. </p>
     *
     * <p> {@code Authorization} 헤더가 비어있거나, {@value AUTHORIZATION_PREFIX}로 시작하지 않는 경우
     *     바로 다음 필터를 호출하여 이후 {@link CustomAuthenticationEntryPoint}에 의해 에러가 처리되도록 한다. </p>
     *
     * <p> 파싱한 {@link AccessToken}을 통해 회원을 조회하고, 인증 정보를 등록한다. </p>
     * <p> 파싱 및 회원 정보 조회 과정에서 발생하는 {@link CustomException}을 예외 처리를 통해 예외 로깅 후
     *     {@link SecurityErrorResponseWriter}를 통해 예외 응답을 설정한다. </p>
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param filterChain 필터 체인
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        
        if (authorization == null || !authorization.startsWith(AUTHORIZATION_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            AccessToken accessToken = parseAccessToken(authorization);
            AuthenticatedMember member = authenticationUsecase.extractMemberIdFromAccessToken(accessToken);
            UserDetails userDetails = new CustomUserDetails(member.memberId(), member.role());
            
            setAuthentication(userDetails);
        } catch (CustomException e) {
            log.warn("[Error/Security] Access Token 인증에 실패하였습니다. ({}) - {} / {}",
                    e.getMessage(), request.getMethod(), request.getRequestURI());
            responseWriter.write(response, e.getErrorType());
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * {@code Authorization} 헤더의 값에서 Access Token을 추출하는 메서드
     *
     * @param authorization {@code Authorization} 헤더의 값
     * @return Access Token
     */
    private AccessToken parseAccessToken(String authorization) {
        String token = authorization.substring(AUTHORIZATION_PREFIX.length()).strip();
        
        if (token.isEmpty()) {
            throw new CustomException(BusinessAuthErrorType.INVALID_TOKEN, "Access Token이 비어있습니다.");
        }
        
        return new AccessToken(token);
    }
    
    /**
     * 인증 정보를 {@link SecurityContext}에 등록하는 메서드
     *
     * @param userDetails 인증 정보 ({@link UserDetails}) 객체
     */
    private void setAuthentication(UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authToken);
        SecurityContextHolder.setContext(context);
    }
}

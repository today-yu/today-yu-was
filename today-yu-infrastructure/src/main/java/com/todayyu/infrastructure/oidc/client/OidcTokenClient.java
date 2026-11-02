package com.todayyu.infrastructure.oidc.client;

import com.todayyu.application.auth.error.BusinessAuthErrorType;
import com.todayyu.common.exception.CustomException;
import com.todayyu.infrastructure.oidc.dto.OidcTokenResponse;
import com.todayyu.infrastructure.oidc.properties.OidcProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class OidcTokenClient {
    private final RestClient oidcRestClient;
    
    private static final String GRANT_TYPE_KEY = "grant_type";
    private static final String GRANT_TYPE_VALUE = "authorization_code";
    private static final String CLIENT_ID_KEY = "client_id";
    private static final String REDIRECT_URI_KEY = "redirect_uri";
    private static final String CODE_KEY = "code";
    private static final String CLIENT_SECRET_KEY = "client_secret";
    
    public OidcTokenClient(@Qualifier(value = "oidcRestClient") RestClient oidcRestClient) {
        this.oidcRestClient = oidcRestClient;
    }
    
    /**
     * 인증 제공자로부터 발급받은 인가 코드를 ID Token으로 교환하는 메서드
     *
     * <p> 토큰 발급 API가 에러 상태 코드(4xx, 5xx)를 응답하거나, 인증 제공자 서버에 연결할 수 없거나,
     * 응답에 ID Token이 존재하지 않는 경우 예외를 발생시킨다. </p>
     *
     * @param properties        인증 제공자의 OIDC 인증 관련 프로퍼티
     * @param authorizationCode 인증 제공자로부터 발급받은 인가 코드
     * @return ID Token이 포함된 토큰 응답
     * @throws CustomException {@link BusinessAuthErrorType#FAIL_SOCIAL_SIGNIN} - 인가 코드 교환에 실패한 경우
     */
    public OidcTokenResponse exchange(OidcProperties.Provider properties, String authorizationCode) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(GRANT_TYPE_KEY, GRANT_TYPE_VALUE);
        body.add(CLIENT_ID_KEY, properties.clientId());
        body.add(REDIRECT_URI_KEY, properties.redirectUri());
        body.add(CODE_KEY, authorizationCode);
        body.add(CLIENT_SECRET_KEY, properties.clientSecret());
        
        try {
            OidcTokenResponse response = oidcRestClient.post()
                    .uri(properties.tokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, clientResponse) -> {
                        throw new CustomException(
                                BusinessAuthErrorType.FAIL_SOCIAL_SIGNIN,
                                "인가 코드 교환에 실패하였습니다. status " + clientResponse.getStatusCode()
                        );
                    })
                    .body(OidcTokenResponse.class);
            
            if (response == null || response.idToken() == null) {
                throw new CustomException(BusinessAuthErrorType.FAIL_SOCIAL_SIGNIN, "ID 토큰이 발급되지 않았습니다.");
            }
            
            return response;
        } catch (RestClientException e) {
            throw new CustomException(BusinessAuthErrorType.FAIL_SOCIAL_SIGNIN, "인증 제공자 서버에 연결할 수 없습니다.", e);
        }
    }
}

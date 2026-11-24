package com.todayyu.infrastructure.kakao.client;

import com.todayyu.application.auth.error.BusinessAuthErrorType;
import com.todayyu.common.exception.CustomException;
import com.todayyu.infrastructure.kakao.properties.KakaoProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;

@Component
public class KakaoUnlinkClient {
    private final RestClient restClient;
    private final String unlinkUrl;
    private final String adminKey;
    
    private static final String TARGET_ID_TYPE_KEY = "target_id_type";
    private static final String TARGET_ID_TYPE_VALUE = "user_id";
    private static final String TARGET_ID_KEY = "target_id";
    
    public KakaoUnlinkClient(
            @Qualifier(value = "kakaoRestClient") RestClient restClient,
            KakaoProperties properties
    ) {
        this.restClient = restClient;
        this.unlinkUrl = properties.getUnlinkUrl();
        this.adminKey = "KakaoAK " + properties.getAdminKey();
    }
    
    /**
     * 카카오 회원번호로 카카오에서 서비스와 사용자의 연결을 해제하는 메서드
     *
     * <p> 서비스 앱 어드민 키(Admin Key) 방식으로 카카오 연결 끊기 API를 호출한다. </p>
     *
     * <p> 연결 끊기 API가 에러 상태 코드(4xx, 5xx)를 응답하거나, 카카오 서버에 연결할 수 없는 경우 예외를 발생시킨다. </p>
     *
     * @param userId 연결을 해제할 사용자의 카카오 회원번호
     * @throws CustomException {@link BusinessAuthErrorType#FAIL_SOCIAL_UNLINK} - 연결 해제에 실패한 경우
     */
    public void unlink(String userId) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(TARGET_ID_TYPE_KEY, TARGET_ID_TYPE_VALUE);
        body.add(TARGET_ID_KEY, userId);
        
        try {
            restClient.post()
                    .uri(unlinkUrl)
                    .header(HttpHeaders.AUTHORIZATION, adminKey)
                    .contentType(new MediaType(MediaType.APPLICATION_FORM_URLENCODED, StandardCharsets.UTF_8))
                    .body(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, ((request, clientResponse) -> {
                        throw new CustomException(
                                BusinessAuthErrorType.FAIL_SOCIAL_UNLINK,
                                "카카오 소셜 연결 해제에 실패하였습니다. statusCode = "
                                        + clientResponse.getStatusCode() + " / authProviderUserId = " + userId
                        );
                    }))
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new CustomException(
                    BusinessAuthErrorType.FAIL_SOCIAL_UNLINK,
                    "카카오 인증 서버에 연결할 수 없습니다. authProviderUserId = " + userId,
                    e
            );
        }
    }
}

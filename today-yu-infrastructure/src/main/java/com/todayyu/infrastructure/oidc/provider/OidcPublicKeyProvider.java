package com.todayyu.infrastructure.oidc.provider;

import com.todayyu.domain.member.vo.AuthProvider;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Jwks;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class OidcPublicKeyProvider {
    private final RestClient oidcRestClient;
    /* 인증 제공자(AuthProvider) 별 공개키 목록을 저장하는 Map */
    private final Map<AuthProvider, CachedKeys> cachedKeysMap = new ConcurrentHashMap<>();
    
    /* 공개키 목록 저장 TTL */
    private static final Duration CACHE_TTL = Duration.ofHours(24);
    /* 공개키 목록 갱신 최소 기간 */
    private static final Duration MIN_FETCH_PUBLIC_KEYS_INTERVAL = Duration.ofMinutes(1);
    
    public OidcPublicKeyProvider(@Qualifier(value = "oidcRestClient") RestClient oidcRestClient) {
        this.oidcRestClient = oidcRestClient;
    }
    
    /**
     * ID Token의 Key ID({@code kid})를 통해 공개키를 반환하는 메서드
     *
     * <p> 해당 인증 제공자({@link AuthProvider})의 공개키 목록이 존재하지 않거나, 공개키 목록 캐시의 TTL이 만료되었거나,
     * 공개키 목록 내 해당 {@code kid}가 존재하지 않는 경우 {@link #fetchPublicKeys(AuthProvider, String)}를 호출하여
     * 해당 인증 제공자의 공개키 목록을 받아온다. </p>
     *
     * <p> 이후, 공개키 목록에 {@code kid}가 존재하지 않을 경우 예외를 발생시킨다. </p>
     *
     * @param provider  인증 제공자
     * @param jwkSetUri 공개키 목록 조회 URI
     * @param kid       공개키 ID
     * @return JWK
     */
    public Key getPublicKey(AuthProvider provider, String jwkSetUri, String kid) {
        KeyId keyId = new KeyId(kid);
        CachedKeys cachedKeys = cachedKeysMap.get(provider);
        
        // 해당 AuthProvider의 공개키 목록이 없거나, 캐시 TTL이 만료되었거나, 공개키 목록 내에 해당 kid가 없는 경우
        if (cachedKeys == null || cachedKeys.isExpired(CACHE_TTL) || cachedKeys.getKeyById(keyId) == null) {
            cachedKeys = fetchPublicKeys(provider, jwkSetUri);
        }
        
        Key key = cachedKeys.getKeyById(keyId);
        
        // 공개키 목록에 kid 존재하지 않는 경우
        if (key == null) {
            throw new JwtException("kid에 해당하는 공개키가 존재하지 않습니다. kid=" + kid);
        }
        
        return key;
    }
    
    /**
     * 인증 제공자의 공개키 목록 조회 API를 통해 공개키를 받아오는 메서드
     *
     * <p> {@code synchronized} 키워드를 통해 임계 영역으로 설정하여 동시 접근으로 인한 중복 공개키 목록 조회 API
     * 호출을 제한한다. </p>
     *
     * <p> 임계 영역에 진입한 뒤, 인증 제공자의 공개키 목록이 존재하며 만료되지 않았고, 갱신 가능 시각이 지나지 않은
     * 경우 현재 공개키 목록을 반환한다. </p>
     *
     * <p> 그렇지 않은 경우, {@code jwkSetUri}를 통하여 해당 인증 제공자의 공개키 목록을 조회하여 저장 후 반환한다. </p>
     *
     * @param provider  인증 제공자
     * @param jwkSetUri 공개키 목록 조회 URI
     * @return 저장된 공개키 목록
     * @throws JwtException 인증 제공자의 공개키 목록 조회 API 호출에 실패한 경우
     */
    private synchronized CachedKeys fetchPublicKeys(AuthProvider provider, String jwkSetUri) {
        CachedKeys currentCachedKeys = cachedKeysMap.get(provider);
        
        if (currentCachedKeys != null
                && !currentCachedKeys.isExpired(CACHE_TTL)
                && !currentCachedKeys.canUpdate(MIN_FETCH_PUBLIC_KEYS_INTERVAL)
        ) {
            return currentCachedKeys;
        }
        
        String body = null;
        
        try {
            body = oidcRestClient.get()
                    .uri(jwkSetUri)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException e) {
            throw new JwtException("인증 제공자의 공개키 목록 조회 API 호출에 실패하였습니다. provider = " + provider, e);
        }
        
        Map<KeyId, Key> keyMap = parsePublicKeys(provider, body);
        
        CachedKeys cachedKeys = new CachedKeys(provider, keyMap, Instant.now());
        cachedKeysMap.put(provider, cachedKeys);
        
        return cachedKeys;
    }
    
    /**
     * 인증 제공자의 공개키 목록 응답을 {@code KeyId}별 공개키로 변환하는 메서드
     *
     * <p> 응답 형식이 올바르지 않아 파싱이 불가능한 경우 예외를 발생시킨다. </p>
     *
     * @param provider 인증 제공자
     * @param body     공개키 목록 응답
     * @return {@code KeyId}별 공개키
     * @throws JwtException 공개키 목록을 파싱할 수 없는 경우
     */
    private Map<KeyId, Key> parsePublicKeys(AuthProvider provider, String body) {
        try {
            return Jwks.setParser()
                    .ignoreUnsupported(true)
                    .build()
                    .parse(body)
                    .getKeys().stream()
                    .collect(Collectors.toMap(jwk -> new KeyId(jwk.getId()), jwk -> jwk.toKey()));
        } catch (JwtException | IllegalArgumentException | IllegalStateException e) {
            throw new JwtException("인증 제공자의 공개키 목록을 파싱할 수 없습니다. provider = " + provider, e);
        }
    }
    
    /**
     * 공개키의 {@code KeyId}를 나타내는 클래스
     */
    private static class KeyId {
        private final String value;
        
        public KeyId(String value) {
            if (value == null || value.isBlank()) {
                throw new JwtException("ID Token 헤더 내 kid가 존재하지 않습니다.");
            }
            
            this.value = value;
        }
        
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof KeyId keyId)) return false;
            return Objects.equals(value, keyId.value);
        }
        
        @Override
        public int hashCode() {
            return Objects.hashCode(value);
        }
    }
    
    /**
     * 캐싱된 공개키 목록을 나타내는 클래스
     */
    private static class CachedKeys {
        private final AuthProvider provider;
        private final Map<KeyId, Key> keyMap;
        private final Instant updatedAt;
        
        public CachedKeys(AuthProvider provider, Map<KeyId, Key> keyMap, Instant updatedAt) {
            this.provider = provider;
            this.keyMap = keyMap;
            this.updatedAt = updatedAt;
        }
        
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof CachedKeys cachedKeys)) return false;
            return provider == cachedKeys.provider;
        }
        
        @Override
        public int hashCode() {
            return Objects.hashCode(provider);
        }
        
        /**
         * 공개키 목록이 {@link #updatedAt}을 기준으로 {@code duration}만큼 지나
         * 만료되었는지 여부를 반환하는 메서드
         *
         * @param duration 기한
         * @return 갱신 이후 {@code duration} 기간만큼 지나 만료되었는지 여부
         */
        public boolean isExpired(Duration duration) {
            return updatedAt.plus(duration).isBefore(Instant.now());
        }
        
        /**
         * 공개키 목록이 {@link #updatedAt}을 기준으로 {@code duration}만큼 지나
         * 업데이트 가능한지 여부를 반환하는 메서드
         *
         * @param duration 기한
         * @return 갱신 이후 {@code duration} 기간만큼 지나 갱신이 가능한지 여부
         */
        public boolean canUpdate(Duration duration) {
            return updatedAt.plus(duration).isBefore(Instant.now());
        }
        
        /**
         * {@code keyId}를 통해 해당 공개키 목록에 존재하는 공개키를 반환하는 메서드
         *
         * @param keyId 공개키 ID
         * @return 공개키
         */
        public Key getKeyById(KeyId keyId) {
            return keyMap.get(keyId);
        }
    }
}

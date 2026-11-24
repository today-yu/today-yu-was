package com.todayyu.infrastructure.auth.adapter.social;

import com.todayyu.application.auth.port.SocialUnlinkPort;
import com.todayyu.domain.member.vo.AuthProvider;
import com.todayyu.domain.member.vo.AuthProviderUserId;
import com.todayyu.domain.member.vo.SocialIdentity;
import com.todayyu.infrastructure.common.annotation.Adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Adapter
public class SocialUnlinkAdapter implements SocialUnlinkPort {
    private final Map<AuthProvider, SocialUnlinkStrategy> socialUnlinkStrategyMap = new HashMap<>();
    
    public SocialUnlinkAdapter(List<SocialUnlinkStrategy> socialUnlinkStrategies) {
        for (SocialUnlinkStrategy strategy : socialUnlinkStrategies) {
            socialUnlinkStrategyMap.put(strategy.getAuthProvider(), strategy);
        }
    }
    
    /**
     * 소셜 인증 정보({@link SocialIdentity})의 인증 제공자에서 서비스와 사용자의 연결을 해제하는 메서드
     *
     * <p> 인증 제공자({@link AuthProvider})에 해당하는 연결 해제 전략({@link SocialUnlinkStrategy})을 찾아,
     *     인증 제공자 내 사용자 ID({@link AuthProviderUserId})로 연결 해제를 위임한다. </p>
     *
     * <p> 연결 해제 전략에서 발생한 예외는 별도로 처리하지 않고 그대로 전파한다. </p>
     *
     * @param socialIdentity 연결을 해제할 소셜 인증 정보
     * @throws IllegalStateException 인증 제공자에 해당하는 연결 해제 전략이 존재하지 않는 경우
     */
    @Override
    public void unlink(SocialIdentity socialIdentity) {
        AuthProvider authProvider = socialIdentity.getAuthProvider();
        AuthProviderUserId authProviderUserId = socialIdentity.getAuthProviderUserId();
        
        SocialUnlinkStrategy strategy = socialUnlinkStrategyMap.get(authProvider);
        
        if (strategy == null) {
            throw new IllegalStateException("인증 제공자의 연결 해제 전략이 존재하지 않습니다. authProvider = "
                    + authProvider + " / authProviderUserId = " + authProviderUserId.getValue());
        }
        
        strategy.unlink(authProviderUserId);
    }
}

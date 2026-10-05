package com.todayyu.application.auth.usecase;

import com.todayyu.application.auth.dto.IssuedAccessToken;
import com.todayyu.application.auth.dto.IssuedRefreshToken;
import com.todayyu.application.auth.dto.SigninCommand;
import com.todayyu.application.auth.dto.SigninResult;
import com.todayyu.application.auth.error.BusinessAuthErrorType;
import com.todayyu.application.auth.port.TokenPort;
import com.todayyu.application.auth.port.save.SaveRefreshTokenPort;
import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.member.port.PasswordPort;
import com.todayyu.application.member.port.read.ReadMemberPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.auth.vo.SessionId;
import com.todayyu.domain.member.entity.Member;
import com.todayyu.domain.member.vo.EncodedPassword;
import com.todayyu.domain.member.vo.Password;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Optional;

@Usecase
@RequiredArgsConstructor
public class SigninUsecase {
    private final ReadMemberPort readMemberPort;
    private final PasswordPort passwordPort;
    private final TokenPort tokenPort;
    private final SaveRefreshTokenPort saveRefreshTokenPort;
    
    /**
     * 로그인 메서드
     *
     * <p> 로그인에 실패하는 경우,
     *     보안을 위해 사용자 응답 예외 에러 타입은 {@link BusinessAuthErrorType#FAIL_SIGNIN}으로 통일한다. </p>
     *
     * <p> 또한, 보안을 위해 비밀번호 불일치 에러가 아니더라도 {@link PasswordPort#matchesDummy(Password)}를 호출하여
     *     Dummy 비밀번호 검증을 실시한다. </p>
     *
     * <p> 로그인에 성공하면 인증 토큰을 발급하며, 그 중 Refresh Token을 저장한다. </p>
     *
     * @param command 로그인 요청 DTO
     * @return 로그인 결과 {@link SigninResult} 객체
     */
    public SigninResult signin(SigninCommand command) {
        Optional<Member> optionalMember = readMemberPort.readByEmail(command.email());
        
        // 해당 이메일로 가입된 계정이 존재하지 않는 경우
        if (optionalMember.isEmpty()) {
            passwordPort.matchesDummy(command.password());
            throw new CustomException(BusinessAuthErrorType.FAIL_SIGNIN, "해당 이메일로 된 계정이 존재하지 않습니다. email = " + command.email());
        }
        
        Member member = optionalMember.get();
        Optional<EncodedPassword> optionalEncodedPassword = member.getEncodedPassword();
        
        // 해당 이메일과 연동돤 로컬 계정(LocalAccount)이 존재하지 않는 경우
        if (optionalEncodedPassword.isEmpty()) {
            passwordPort.matchesDummy(command.password());
            throw new CustomException(BusinessAuthErrorType.FAIL_SIGNIN, "로컬 계정이 존재하지 않습니다. email = " + command.email());
        }
        
        // 비밀번호가 일치하지 않는 경우
        if (!passwordPort.matches(command.password(), optionalEncodedPassword.get())) {
            throw new CustomException(BusinessAuthErrorType.FAIL_SIGNIN, "비밀번호가 일치하지 않습니다. email = " + command.email());
        }
        
        Instant now = Instant.now();
        SessionId sessionId = SessionId.create();
        IssuedAccessToken issuedAccessToken = tokenPort.issueAccessToken(member.getId(), member.getRole(), sessionId, now);
        IssuedRefreshToken issuedRefreshToken = tokenPort.issueRefreshToken(member.getId(), sessionId, now);
        
        saveRefreshTokenPort.save(member.getId(), issuedRefreshToken);
        
        return SigninResult.from(issuedAccessToken, issuedRefreshToken);
    }
}

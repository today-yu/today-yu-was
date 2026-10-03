package com.todayyu.domain.member.entity;

import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.member.error.MemberErrorType;
import com.todayyu.domain.member.vo.Email;
import com.todayyu.domain.member.vo.VerificationCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class EmailVerification {
    private Email email;
    private VerificationCode code;
    private LocalDateTime issuedAt;
    
    public static final Long EMAIL_VERIFICATION_DURATION_MINUTE = 3L;
    private static final Long REISSUE_POSSIBLE_OFFSET_MINUTE = 1L;
    
    public EmailVerification(Email email, VerificationCode code, LocalDateTime issuedAt) {
        if (email == null) {
            throw new IllegalArgumentException("이메일은 비어있을 수 없습니다.");
        }
        
        if (code == null) {
            throw new IllegalArgumentException("인증 코드는 비어있을 수 없습니다.");
        }
        
        if (issuedAt == null) {
            throw new IllegalArgumentException("인증 코드 발급 시각은 비어있을 수 없습니다.");
        }
        
        this.email = email;
        this.code = code;
        this.issuedAt = issuedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EmailVerification that)) return false;
        return Objects.equals(email, that.email);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(email);
    }
    
    /**
     * 이메일 인증 코드 재발급 메서드
     *
     * <p> 재발급 요청 시각이 이메일 재발급 제한 시각({@link #getReissuePossibleTime()}) 이전일 경우엔 재발급을 제한한다. </p>
     * <p> 그렇지 않을 경우, 새로운 인증 코드와 발급 시각으로 갱신한 이메일 인증 객체를 반환한다. </p>
     *
     * @param code 이메일 인증 코드
     * @param now 재발급 요청 시각
     * @return 이메일 인증 객체
     * @throws CustomException 이메일 재발급 제한 시간 내 재발급 요청을 한 경우 ({@link MemberErrorType#VERIFICATION_CODE_REISSUE_TOO_EARLY})
     */
    public EmailVerification reissue(VerificationCode code, LocalDateTime now) {
        if (now.isBefore(getReissuePossibleTime())) {
            throw new CustomException(MemberErrorType.VERIFICATION_CODE_REISSUE_TOO_EARLY);
        }
        
        return new EmailVerification(email, code, now);
    }
    
    /**
     * 이메일 인증 코드 재발급 가능 시각을 반환하는 메서드
     *
     * <p> 발급 시각({@link #issuedAt}) 이후 {@value #REISSUE_POSSIBLE_OFFSET_MINUTE} 시간(분)이 지난 시점 이후로 재발급이 가능하다. </p>
     *
     * @return 이메일 인증 코드 재발급 가능 시각
     */
    private LocalDateTime getReissuePossibleTime() {
        return issuedAt.plusMinutes(REISSUE_POSSIBLE_OFFSET_MINUTE);
    }
    
    /**
     * 이메일 인증 코드 만료 시각을 반환하는 메서드
     *
     * <p> 발급 시각({@link #issuedAt}) 이후 {@value #EMAIL_VERIFICATION_DURATION_MINUTE} 시간(분)까지만 유효하다.</p>
     *
     * @return 이메일 인증 코드 만료 시각
     */
    public LocalDateTime getExpiredAt() {
        return issuedAt.plusMinutes(EMAIL_VERIFICATION_DURATION_MINUTE);
    }
    
    /**
     * 이메일 인증 코드 일치 여부를 검증하는 메서드
     *
     * @param code 입력된 이메일 인증 코드
     * @return 인증 코드 일치 여부
     */
    public boolean verify(VerificationCode code) {
        return code.equals(this.code);
    }
}

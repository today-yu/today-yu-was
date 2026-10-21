package com.todayyu.domain.member.entity;

import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.member.error.MemberErrorType;
import com.todayyu.domain.member.vo.VerificationCode;
import com.todayyu.domain.member.vo.VerificationFailCount;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class PasswordResetVerification {
    private Email email;
    private VerificationCode code;
    private LocalDateTime issuedAt;
    private VerificationFailCount failCount;
    
    private static final int MAX_FAIL_COUNT = 5;
    private static final int EXPIRATION_MINUTE = 5;
    private static final int REISSUE_POSSIBLE_OFFSET_MINUTE = 1;
    
    private PasswordResetVerification(
            Email email,
            VerificationCode code,
            LocalDateTime issuedAt,
            VerificationFailCount failCount
    ) {
        if (email == null) {
            throw new IllegalArgumentException("이메일은 비어있을 수 없습니다.");
        }
        
        if (code == null) {
            throw new IllegalArgumentException("인증 코드는 비어있을 수 없습니다.");
        }
        
        if (issuedAt == null) {
            throw new IllegalArgumentException("발급 시각은 비어있을 수 없습니다.");
        }
        
        if (failCount == null) {
            throw new IllegalArgumentException("실패 횟수는 비어있을 수 없습니다.");
        }
        
        this.email = email;
        this.code = code;
        this.issuedAt = issuedAt;
        this.failCount = failCount;
    }
    
    public static PasswordResetVerification issue(Email email, VerificationCode code, LocalDateTime issuedAt) {
        return new PasswordResetVerification(email, code, issuedAt, VerificationFailCount.zero());
    }
    
    public static PasswordResetVerification of(
            Email email,
            VerificationCode code,
            LocalDateTime issuedAt,
            VerificationFailCount failCount
    ) {
        return new PasswordResetVerification(email, code, issuedAt, failCount);
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PasswordResetVerification that)) return false;
        return Objects.equals(email, that.email);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(email);
    }
    
    /**
     * 비밀번호 초기화 인증 코드 검증 메서드
     *
     * <p> 최대 실패 횟수에 도달한 인증이라면 예외를 발생시킨다. </p>
     *
     * <p> 인증 코드가 일치하지 않을 경우 인증 실패 횟수를 증가시킨다. </p>
     *
     * @param code 입력된 인증 코드
     * @return 인증 코드 일치 여부
     * @throws CustomException {@link MemberErrorType#EXCEED_VERIFICATION_FAIL_COUNT}
     *                         - 최대 실패 횟수({@value #MAX_FAIL_COUNT})에 도달한 인증을 검증하는 경우
     */
    public boolean verify(VerificationCode code) {
        if (isExceededFailCount()) {
            throw new CustomException(MemberErrorType.EXCEED_VERIFICATION_FAIL_COUNT);
        }
        
        boolean matched = this.code.verify(code);
        
        if (!matched) {
            failCount = failCount.increase();
        }
        
        return matched;
    }
    
    /**
     * 인증 코드 검증 최대 실패 횟수({@value #MAX_FAIL_COUNT}) 도달 여부를 반환하는 메서드
     *
     * @return 인증 코드 최대 실패 횟수 도달 여부
     */
    private boolean isExceededFailCount() {
        return failCount.isReached(MAX_FAIL_COUNT);
    }
    
    /**
     * 해당 인증의 만료 시각을 반환하는 메서드
     *
     * <p> 발급 시각({@link #issuedAt})을 기준으로 {@value #EXPIRATION_MINUTE} 분 뒤 만료된다. </p>
     *
     * @return 인증 만료 시각
     */
    public LocalDateTime getExpiredAt() {
        return issuedAt.plusMinutes(EXPIRATION_MINUTE);
    }
    
    /**
     * 비밀번호 초기화 인증 재발급 메서드
     *
     * <p> 기존 비밀번호 초기화 인증이 존재하는 경우 재발급을 시도할 때 사용한다. </p>
     *
     * <p> {@link #getReissuePossibleTime()} 메서드를 호출하여 기존 인증 발급 후 재발급 가능한 시각이
     *     도달하지 않았을 경우에 예외를 발생시킨다. </p>
     *
     * <p> 새로운 비밀번호 초기화 인증 객체 발급 시 실패 횟수({@link #failCount})는 0으로 설정한다. </p>
     *
     * @param code 새로 발급한 인증 코드
     * @param now 현재 시각
     * @return 새로 발급한 비밀번호 초기화 인증 객체
     * @throws CustomException {@link MemberErrorType#VERIFICATION_CODE_REISSUE_TOO_EARLY}
     *                         - 재발급 가능 시각 이전에 재발급을 요청한 경우
     */
    public PasswordResetVerification reissue(VerificationCode code, LocalDateTime now) {
        if (now.isBefore(getReissuePossibleTime())) {
            throw new CustomException(MemberErrorType.VERIFICATION_CODE_REISSUE_TOO_EARLY);
        }
        
        return new PasswordResetVerification(email, code, now, VerificationFailCount.zero());
    }
    
    /**
     * 재발급 가능한 시각을 반환하는 메서드
     *
     * <p> 발급 시각({@link #issuedAt}) 기준으로 {@value #REISSUE_POSSIBLE_OFFSET_MINUTE} 분
     *     이후부터 재발급 가능하다. </p>
     *
     * @return 재발급 가능 시각
     */
    private LocalDateTime getReissuePossibleTime() {
        return issuedAt.plusMinutes(REISSUE_POSSIBLE_OFFSET_MINUTE);
    }
}

package com.todayyu.domain.member.vo;

import lombok.Getter;

import java.util.Objects;

@Getter
public class VerificationFailCount {
    private final Integer value;
    
    private VerificationFailCount(Integer value) {
        if (value == null) {
            throw new IllegalArgumentException("인증 실패 횟수는 비어있을 수 없습니다.");
        }
        
        if (value < 0) {
            throw new IllegalArgumentException("인증 실패 횟수는 0 이상이어야 합니다.");
        }
        
        this.value = value;
    }
    
    public static VerificationFailCount zero() {
        return new VerificationFailCount(0);
    }
    
    public static VerificationFailCount of(Integer value) {
        return new VerificationFailCount(value);
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VerificationFailCount that)) return false;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
    
    /**
     * 인증 실패 횟수를 증가시키는 메서드
     *
     * <p> 인증 실패 횟수를 {@code 1} 증가시킨다. </p>
     *
     * @return 실패 횟수가 증가된 인증 실패 횟수 객체
     */
    public VerificationFailCount increase() {
        return new VerificationFailCount(value + 1);
    }
    
    /**
     * 인증 실패 횟수가 {@code target}에 도달했는지 여부를 반환하는 메서드
     *
     * @param target 도달 확인 대상 횟수
     * @return 인증 실패 횟수 도달 여부
     */
    public boolean isReached(int target) {
        return value >= target;
    }
}

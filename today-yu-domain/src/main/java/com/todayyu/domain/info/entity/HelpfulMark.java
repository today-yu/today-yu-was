package com.todayyu.domain.info.entity;

import com.todayyu.domain.info.vo.HelpfulMarkId;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;
import lombok.Getter;

import java.util.Objects;

@Getter
public class HelpfulMark {
    private HelpfulMarkId id;
    private InfoId infoId;
    private MemberId memberId;
    
    private HelpfulMark(HelpfulMarkId id, InfoId infoId, MemberId memberId) {
        this.id = id;
        this.infoId = infoId;
        this.memberId = memberId;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HelpfulMark that)) return false;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
    
    public static HelpfulMark withoutId(InfoId infoId, MemberId memberId) {
        validateInfoIdAndMemberId(infoId, memberId);
        return new HelpfulMark(null, infoId, memberId);
    }
    
    public static HelpfulMark withId(HelpfulMarkId id, InfoId infoId, MemberId memberId) {
        if (id == null) {
            throw new IllegalArgumentException("'도움이 되는 정보에요' ID는 비어있을 수 없습니다.");
        }
        
        validateInfoIdAndMemberId(infoId, memberId);
        return new HelpfulMark(id, infoId, memberId);
    }
    
    /**
     * '도움이 되는 정보에요' 생성 시 정보 ID와 회원 ID가 유효한지 검사하는 정적 메서드
     *
     * @param infoId 정보 ID
     * @param memberId 회원 ID
     * @throws IllegalArgumentException 정보 ID 또는 회원 ID가 비어있을 경우
     */
    private static void validateInfoIdAndMemberId(InfoId infoId, MemberId memberId) {
        if (infoId == null) {
            throw new IllegalArgumentException("정보 ID는 비어있을 수 없습니다.");
        }
        
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 비어있을 수 없습니다.");
        }
    }
}

package com.todayyu.domain.scrap.entity;

import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.scrap.vo.ScrapId;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class Scrap {
    private ScrapId id;
    private InfoId infoId;
    private MemberId memberId;
    private LocalDateTime scrappedAt;
    
    private Scrap(ScrapId id, InfoId infoId, MemberId memberId, LocalDateTime scrappedAt) {
        this.id = id;
        this.infoId = infoId;
        this.memberId = memberId;
        this.scrappedAt = scrappedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Scrap scrap)) return false;
        return Objects.equals(id, scrap.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
    
    public static Scrap withoutId(InfoId infoId, MemberId memberId, LocalDateTime scrappedAt) {
        validateRequiredFields(infoId, memberId, scrappedAt);
        
        return new Scrap(null, infoId, memberId, scrappedAt);
    }
    
    public static Scrap withId(ScrapId id, InfoId infoId, MemberId memberId, LocalDateTime scrappedAt) {
        if (id == null) {
            throw new IllegalArgumentException("스크랩 ID는 비어있을 수 없습니다.");
        }
        
        validateRequiredFields(infoId, memberId, scrappedAt);
        
        return new Scrap(id, infoId, memberId, scrappedAt);
    }

    /**
     * 필수 입력 필드에 대한 검사 정적 메서드
     *
     * @param infoId     스크랩한 정보 ID
     * @param memberId   스크랩한 회원 ID
     * @param scrappedAt 스크랩 시각
     * @throws IllegalArgumentException 필수 입력 필드가 누락된 경우
     * <ul>
     *     <li> {@code infoId}가 비어있는 경우 </li>
     *     <li> {@code memberId}가 비어있는 경우 </li>
     *     <li> {@code scrappedAt}가 비어있는 경우 </li>
     * </ul>
     */
    private static void validateRequiredFields(InfoId infoId, MemberId memberId, LocalDateTime scrappedAt) {
        if (infoId == null) {
            throw new IllegalArgumentException("정보 ID는 비어있을 수 없습니다.");
        }
        
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 비어있을 수 없습니다.");
        }
        
        if (scrappedAt == null) {
            throw new IllegalArgumentException("스크랩 시각은 비어있을 수 없습니다.");
        }
    }
}

package com.todayyu.domain.notice.entity;

import com.todayyu.domain.notice.vo.NoticeContent;
import com.todayyu.domain.notice.vo.NoticeId;
import com.todayyu.domain.notice.vo.NoticeTitle;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class Notice {
    private NoticeId id;
    private NoticeTitle title;
    private NoticeContent content;
    private LocalDateTime publishedAt;
    private LocalDateTime updatedAt;
    
    private Notice(
            NoticeId id,
            NoticeTitle title,
            NoticeContent content,
            LocalDateTime publishedAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.publishedAt = publishedAt;
        this.updatedAt = updatedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Notice notice)) return false;
        return Objects.equals(id, notice.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
    
    public static Notice withoutId(
            NoticeTitle title,
            NoticeContent content,
            LocalDateTime publishedAt
    ) {
        validateRequiredFields(title, content, publishedAt, publishedAt);
        
        return new Notice(null, title, content, publishedAt, publishedAt);
    }
    
    public static Notice withId(
            NoticeId id,
            NoticeTitle title,
            NoticeContent content,
            LocalDateTime publishedAt,
            LocalDateTime updatedAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("공지 ID는 비어있을 수 없습니다.");
        }
        
        validateRequiredFields(title, content, publishedAt, updatedAt);
        
        return new Notice(id, title, content, publishedAt, updatedAt);
    }
    
    /**
     * 공지 생성 시 필수 필드가 모두 존재하는지 검사하는 정적 메서드
     *
     * @param title       공지 제목
     * @param content     공지 내용
     * @param publishedAt 공지 발행 시각
     * @param updatedAt   공지 수정 시각
     * @throws IllegalArgumentException 공지 제목, 내용, 발행 시각, 수정 시각 중 하나라도 {@code null}인 경우
     */
    private static void validateRequiredFields(
            NoticeTitle title,
            NoticeContent content,
            LocalDateTime publishedAt,
            LocalDateTime updatedAt
    ) {
        if (title == null) {
            throw new IllegalArgumentException("공지 제목은 비어있을 수 없습니다.");
        }
        
        if (content == null) {
            throw new IllegalArgumentException("공지 내용은 비어있을 수 없습니다.");
        }
        
        if (publishedAt == null) {
            throw new IllegalArgumentException("공지 발행 시각은 비어있을 수 없습니다.");
        }
        
        if (updatedAt == null) {
            throw new IllegalArgumentException("공지 업데이트 시각은 비어있을 수 없습니다.");
        }
    }
}

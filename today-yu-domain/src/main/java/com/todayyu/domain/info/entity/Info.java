package com.todayyu.domain.info.entity;

import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoContent;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoTitle;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class Info {
    private InfoId id;
    private InfoCategory category;
    private InfoTitle title;
    private InfoContent content;
    private LocalDateTime publishedAt;
    private LocalDateTime updatedAt;
    
    private Info(
            InfoId id,
            InfoCategory category,
            InfoTitle title,
            InfoContent content,
            LocalDateTime publishedAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.content = content;
        this.publishedAt = publishedAt;
        this.updatedAt = updatedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Info info)) return false;
        return Objects.equals(id, info.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
    
    public static Info withoutId(
            InfoCategory category,
            InfoTitle title,
            InfoContent content,
            LocalDateTime publishedAt
    ) {
        validateCategory(category);
        return new Info(null, category, title, content, publishedAt, publishedAt);
    }
    
    public static Info withId(
            InfoId id,
            InfoCategory category,
            InfoTitle title,
            InfoContent content,
            LocalDateTime publishedAt,
            LocalDateTime updatedAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("정보 ID는 비어있을 수 없습니다.");
        }
        
        validateCategory(category);
        return new Info(id, category, title, content, publishedAt, updatedAt);
    }
    
    /**
     * 정보 생성 시 카테고리가 유효한지 검사하는 정적 메서드
     *
     * @param category 정보 카테고리
     * @throws IllegalArgumentException 정보 카테고리가 {@code null}인 경우
     */
    private static void validateCategory(InfoCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("정보 카테고리는 비어있을 수 없습니다.");
        }
    }
}

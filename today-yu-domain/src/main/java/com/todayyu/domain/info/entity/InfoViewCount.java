package com.todayyu.domain.info.entity;

import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.ViewCount;
import lombok.Getter;

import java.util.Objects;

@Getter
public class InfoViewCount {
    private InfoId infoId;
    private ViewCount viewCount;
    
    private InfoViewCount(InfoId infoId, ViewCount viewCount) {
        this.infoId = infoId;
        this.viewCount = viewCount;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InfoViewCount that)) return false;
        return Objects.equals(infoId, that.infoId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(infoId);
    }
    
    public static InfoViewCount init(InfoId infoId) {
        if (infoId == null) {
            throw new IllegalArgumentException("정보 ID는 비어있을 수 없습니다.");
        }
        
        return new InfoViewCount(infoId, new ViewCount(0L));
    }
    
    public static InfoViewCount of(InfoId infoId, ViewCount viewCount) {
        if (infoId == null) {
            throw new IllegalArgumentException("정보 ID는 비어있을 수 없습니다.");
        }
        
        if (viewCount == null) {
            throw new IllegalArgumentException("조회수는 비어있을 수 없습니다.");
        }
        
        return new InfoViewCount(infoId, viewCount);
    }
}

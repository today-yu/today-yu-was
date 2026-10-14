package com.todayyu.application.info.dto.result;

import com.todayyu.domain.info.entity.Info;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoTitle;

import java.time.LocalDateTime;

public record LinkedInfoResult(
        InfoId id,
        InfoCategory category,
        InfoTitle title,
        LocalDateTime updatedAt
) {
    public static LinkedInfoResult from(Info info) {
        return new LinkedInfoResult(
                info.getId(),
                info.getCategory(),
                info.getTitle(),
                info.getUpdatedAt()
        );
    }
}

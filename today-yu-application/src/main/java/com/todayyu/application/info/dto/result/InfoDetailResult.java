package com.todayyu.application.info.dto.result;

import com.todayyu.application.info.port.dto.InfoRegistrationRequestView;
import com.todayyu.application.info.port.dto.InfoUpdateRequestView;
import com.todayyu.domain.info.entity.Info;
import com.todayyu.domain.info.entity.InfoViewCount;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoContent;
import com.todayyu.domain.info.vo.InfoId;
import com.todayyu.domain.info.vo.InfoTitle;
import com.todayyu.domain.info.vo.ViewCount;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Builder
public record InfoDetailResult(
        InfoId id,
        InfoCategory category,
        InfoTitle title,
        InfoContent content,
        LocalDateTime publishedAt,
        LocalDateTime updatedAt,
        ViewCount viewCount,
        long helpfulMarkCount,
        boolean helpfulMarked,
        List<ContributionResult> contributions
) {
    public static InfoDetailResult from(
            Info info,
            InfoViewCount infoViewCount,
            long helpfulMarkCount,
            boolean helpfulMarked,
            List<InfoRegistrationRequestView> infoRegistrationRequestViews,
            List<InfoUpdateRequestView> infoUpdateRequestViews
    ) {
        List<ContributionResult> contributionResults = Stream
                .concat(
                        infoRegistrationRequestViews.stream().map(ContributionResult::from),
                        infoUpdateRequestViews.stream().map(ContributionResult::from)
                )
                .sorted(Comparator.comparing(ContributionResult::requestedAt).reversed())
                .toList();
        
        return InfoDetailResult.builder()
                .id(info.getId())
                .category(info.getCategory())
                .title(info.getTitle())
                .content(info.getContent())
                .publishedAt(info.getPublishedAt())
                .updatedAt(info.getUpdatedAt())
                .viewCount(infoViewCount.getViewCount())
                .helpfulMarkCount(helpfulMarkCount)
                .helpfulMarked(helpfulMarked)
                .contributions(contributionResults)
                .build();
    }
}

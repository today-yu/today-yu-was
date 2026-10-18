package com.todayyu.persistence.notice.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.todayyu.persistence.notice.dto.NoticeSummaryViewProjection;
import com.todayyu.persistence.notice.entity.QNoticeJpaEntity;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CustomNoticeRepositoryImpl implements CustomNoticeRepository {
    private final JPAQueryFactory jpaQueryFactory;
    
    private final QNoticeJpaEntity notice = QNoticeJpaEntity.noticeJpaEntity;
    
    /**
     * 커서 이후의 공지 목록을 최신 발행 순으로 조회하는 메서드
     *
     * <p> 공지 엔티티 전체가 아닌, 목록 조회에 필요한 컬럼만 {@link NoticeSummaryViewProjection}으로 조회한다. </p>
     *
     * <p> 공지 ID는 발행 순서대로 증가하므로, 공지 ID 내림차순을 최신 발행 순으로 사용한다. </p>
     *
     * <ul>
     *     <li> 커서 값({@code cursorId})이 {@code null}이면 처음부터 조회한다. </li>
     * </ul>
     *
     * @param cursorId 마지막으로 조회한 공지의 ID
     * @param limit    최대 조회 개수
     * @return 공지 요약 Projection 목록
     */
    @Override
    public List<NoticeSummaryViewProjection> findByCursor(Long cursorId, int limit) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        NoticeSummaryViewProjection.class,
                        notice.id,
                        notice.title,
                        notice.publishedAt
                ))
                .from(notice)
                .where(afterCursor(cursorId))
                .orderBy(notice.id.desc())
                .limit(limit)
                .fetch();
    }
    
    /**
     * 커서 이후의 공지만 조회하도록 하는 커서 조건을 반환하는 메서드
     *
     * <p> 정렬 조건(공지 ID 내림차순)과 같은 기준·방향으로 비교해야 중복·누락 없이 다음 목록을 조회할 수 있으므로,
     *     커서보다 공지 ID가 작은 공지를 조회한다. </p>
     *
     * @param cursorId 마지막으로 조회한 공지의 ID
     * @return 커서 조건, 커서 값이 {@code null}이면 조건을 적용하지 않도록 {@code null}
     */
    private BooleanExpression afterCursor(Long cursorId) {
        if (cursorId == null) {
            return null;
        }
        
        return notice.id.lt(cursorId);
    }
}

package com.todayyu.persistence.info.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.todayyu.application.info.dto.InfoSortType;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.persistence.info.dto.InfoSummaryViewProjection;
import com.todayyu.persistence.info.entity.QInfoJpaEntity;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class CustomInfoRepositoryImpl implements CustomInfoRepository {
    private final JPAQueryFactory jpaQueryFactory;
    
    private final QInfoJpaEntity info = QInfoJpaEntity.infoJpaEntity;
    
    /**
     * 카테고리({@link InfoCategory})와 정렬 기준({@link InfoSortType})에 따라 커서 이후의 정보 목록을 조회하는 메서드
     *
     * <p> 정보 엔티티 전체가 아닌, 목록 조회에 필요한 컬럼만 {@link InfoSummaryViewProjection}으로 조회한다. </p>
     *
     * <ul>
     *     <li> 카테고리가 {@code null}이면 전체 카테고리를 조회한다. </li>
     *     <li> 커서 값({@code cursorUpdatedAt}, {@code cursorId})이 {@code null}이면 처음부터 조회한다. </li>
     * </ul>
     *
     * @param category        정보 카테고리
     * @param sortType        정렬 기준
     * @param cursorUpdatedAt 마지막으로 조회한 정보의 수정 일시
     * @param cursorId        마지막으로 조회한 정보의 ID
     * @param limit           최대 조회 개수
     * @return 정보 요약 Projection 목록
     */
    @Override
    public List<InfoSummaryViewProjection> findByCursor(
            InfoCategory category,
            InfoSortType sortType,
            LocalDateTime cursorUpdatedAt,
            Long cursorId,
            int limit
    ) {
        return jpaQueryFactory
                .select(
                        Projections.constructor(
                                InfoSummaryViewProjection.class,
                                info.id,
                                info.category,
                                info.title,
                                info.updatedAt
                        )
                )
                .from(info)
                .where(
                        categoryEq(category),
                        afterCursor(sortType, cursorUpdatedAt, cursorId)
                )
                .orderBy(orderBy(sortType))
                .limit(limit)
                .fetch();
    }
    
    /**
     * 카테고리 조건을 반환하는 메서드
     *
     * @param category 정보 카테고리
     * @return 카테고리 조건, 카테고리가 {@code null}이면 조건을 적용하지 않도록 {@code null}
     */
    private BooleanExpression categoryEq(InfoCategory category) {
        return category == null ? null : info.category.eq(category);
    }

    /**
     * 정렬 기준에 따라 커서 이후의 정보만 조회하도록 하는 커서 조건을 반환하는 메서드
     *
     * <p> {@code updatedAt}이 같은 정보가 여러 개일 수 있으므로, {@code (updatedAt, id)} 순으로 비교한다.
     *     정렬 조건({@link #orderBy})과 같은 기준·방향으로 비교해야 중복·누락 없이 다음 목록을 조회할 수 있다. </p>
     *
     * <ul>
     *     <li> {@code LATEST}: 커서보다 수정 일시가 이전이거나, 수정 일시가 같고 ID가 작은 정보 </li>
     *     <li> {@code OLDEST}: 커서보다 수정 일시가 이후이거나, 수정 일시가 같고 ID가 큰 정보 </li>
     * </ul>
     *
     * @param sortType        정렬 기준
     * @param cursorUpdatedAt 마지막으로 조회한 정보의 수정 일시
     * @param cursorId        마지막으로 조회한 정보의 ID
     * @return 커서 조건, 커서 값이 {@code null}이면 조건을 적용하지 않도록 {@code null}
     */
    private BooleanExpression afterCursor(InfoSortType sortType, LocalDateTime cursorUpdatedAt, Long cursorId) {
        if (cursorUpdatedAt == null || cursorId == null) {
            return null;
        }
        
        return switch (sortType) {
            case LATEST -> info.updatedAt.lt(cursorUpdatedAt)
                    .or(info.updatedAt.eq(cursorUpdatedAt).and(info.id.lt(cursorId)));
            case OLDEST -> info.updatedAt.gt(cursorUpdatedAt)
                    .or(info.updatedAt.eq(cursorUpdatedAt).and(info.id.gt(cursorId)));
        };
    }
    
    /**
     * 정렬 기준에 따른 정렬 조건을 반환하는 메서드
     *
     * <p> {@code updatedAt}이 같은 정보가 여러 개일 수 있으므로, 순서를 확정하기 위해 {@code id}를 보조 정렬 기준으로 사용한다. </p>
     *
     * <ul>
     *     <li> {@code LATEST}: 수정 일시 내림차순, ID 내림차순 </li>
     *     <li> {@code OLDEST}: 수정 일시 오름차순, ID 오름차순 </li>
     * </ul>
     *
     * @param sortType 정렬 기준
     * @return 정렬 조건 배열
     */
    private OrderSpecifier<?>[] orderBy(InfoSortType sortType) {
        return switch (sortType) {
            case LATEST -> new OrderSpecifier<?>[]{info.updatedAt.desc(), info.id.desc()};
            case OLDEST -> new OrderSpecifier<?>[]{info.updatedAt.asc(), info.id.asc()};
        };
    }
}

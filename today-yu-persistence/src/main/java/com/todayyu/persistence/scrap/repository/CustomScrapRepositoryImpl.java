package com.todayyu.persistence.scrap.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.todayyu.application.scrap.dto.ScrapSortType;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.persistence.info.entity.QHelpfulMarkJpaEntity;
import com.todayyu.persistence.info.entity.QInfoJpaEntity;
import com.todayyu.persistence.scrap.dto.ScrapViewProjection;
import com.todayyu.persistence.scrap.entity.QScrapJpaEntity;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class CustomScrapRepositoryImpl implements CustomScrapRepository {
    private final JPAQueryFactory jpaQueryFactory;
    
    private final QScrapJpaEntity scrap = QScrapJpaEntity.scrapJpaEntity;
    private final QInfoJpaEntity info = QInfoJpaEntity.infoJpaEntity;
    private final QHelpfulMarkJpaEntity helpfulMark = QHelpfulMarkJpaEntity.helpfulMarkJpaEntity;
    
    /**
     * 회원 ID와 정보 ID 목록을 통해 회원이 스크랩한 정보 ID를 조회하는 메서드
     *
     * @param memberId 회원 ID
     * @param infoIds  정보 ID 목록
     * @return 정보 ID 목록 중 회원이 스크랩한 정보 ID 목록
     */
    @Override
    public List<Long> findInfoIdsByMemberIdAndInfoIds(Long memberId, List<Long> infoIds) {
        return jpaQueryFactory
                .select(scrap.infoId)
                .from(scrap)
                .where(
                        scrap.memberId.eq(memberId),
                        scrap.infoId.in(infoIds)
                )
                .fetch();
    }
    
    /**
     * 카테고리({@link InfoCategory})와 정렬 기준({@link ScrapSortType})에 따라 커서 이후의 회원이 스크랩한 정보 목록을 조회하는 메서드
     *
     * <p> 스크랩과 정보를 조인하여, 목록 조회에 필요한 컬럼만 {@link ScrapViewProjection}으로 조회한다.
     *     각 정보의 '도움이 되는 정보에요' 수는 상관 서브쿼리로 함께 조회하며, 표시가 없으면 0이다. </p>
     *
     * <ul>
     *     <li> 카테고리가 {@code null}이면 전체 카테고리를 조회한다. </li>
     *     <li> 커서 값({@code cursorUpdatedAt}, {@code cursorInfoId})이 {@code null}이면 처음부터 조회한다. </li>
     * </ul>
     *
     * @param memberId        회원 ID
     * @param category        정보 카테고리
     * @param sortType        정렬 기준
     * @param cursorUpdatedAt 마지막으로 조회한 정보의 수정 일시
     * @param cursorInfoId    마지막으로 조회한 정보의 ID
     * @param limit           최대 조회 개수
     * @return 스크랩한 정보 요약 Projection 목록
     */
    @Override
    public List<ScrapViewProjection> findByCursor(
            Long memberId,
            InfoCategory category,
            ScrapSortType sortType,
            LocalDateTime cursorUpdatedAt,
            Long cursorInfoId,
            int limit
    ) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        ScrapViewProjection.class,
                        info.id,
                        info.category,
                        info.title,
                        JPAExpressions
                                .select(helpfulMark.count())
                                .from(helpfulMark)
                                .where(helpfulMark.infoId.eq(info.id)),
                        info.updatedAt
                ))
                .from(scrap)
                .join(info).on(info.id.eq(scrap.infoId))
                .where(
                        scrap.memberId.eq(memberId),
                        categoryEq(category),
                        afterCursor(sortType, cursorUpdatedAt, cursorInfoId)
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
        if (category == null) {
            return null;
        }
        
        return info.category.eq(category);
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
     * @param cursorInfoId    마지막으로 조회한 정보의 ID
     * @return 커서 조건, 커서 값이 {@code null}이면 조건을 적용하지 않도록 {@code null}
     */
    private BooleanExpression afterCursor(ScrapSortType sortType, LocalDateTime cursorUpdatedAt, Long cursorInfoId) {
        if (cursorUpdatedAt == null || cursorInfoId == null) {
            return null;
        }
        
        return switch (sortType) {
            case LATEST -> info.updatedAt.lt(cursorUpdatedAt)
                    .or(info.updatedAt.eq(cursorUpdatedAt).and(info.id.lt(cursorInfoId)));
            case OLDEST -> info.updatedAt.gt(cursorUpdatedAt)
                    .or(info.updatedAt.eq(cursorUpdatedAt).and(info.id.gt(cursorInfoId)));
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
    private OrderSpecifier<?>[] orderBy(ScrapSortType sortType) {
        return switch (sortType) {
            case LATEST -> new OrderSpecifier<?>[]{info.updatedAt.desc(), info.id.desc()};
            case OLDEST -> new OrderSpecifier<?>[]{info.updatedAt.asc(), info.id.asc()};
        };
    }
}

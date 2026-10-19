package com.todayyu.persistence.info.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.todayyu.application.info.dto.InfoUpdateRequestSortType;
import com.todayyu.domain.info.vo.InfoCategory;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.persistence.info.dto.InfoUpdateRequestProjection;
import com.todayyu.persistence.info.dto.InfoUpdateRequestSummaryViewProjection;
import com.todayyu.persistence.info.entity.QInfoJpaEntity;
import com.todayyu.persistence.info.entity.QInfoUpdateRequestJpaEntity;
import com.todayyu.persistence.member.entity.QMemberJpaEntity;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class CustomInfoUpdateRequestRepositoryImpl implements CustomInfoUpdateRequestRepository {
    private final JPAQueryFactory jpaQueryFactory;
    
    private final QInfoUpdateRequestJpaEntity request = QInfoUpdateRequestJpaEntity.infoUpdateRequestJpaEntity;
    private final QMemberJpaEntity member = QMemberJpaEntity.memberJpaEntity;
    private final QInfoJpaEntity info = QInfoJpaEntity.infoJpaEntity;
    
    @Override
    public List<InfoUpdateRequestProjection> findWithRequesterNicknameByInfoIdAndStatus(
            Long infoId,
            InfoRequestStatus status
    ) {
        return jpaQueryFactory
                .select(
                        Projections.constructor(
                                InfoUpdateRequestProjection.class,
                                request,
                                member.nickname
                        )
                )
                .from(request)
                .join(member)
                .on(request.requesterId.eq(member.id))
                .where(
                        request.infoId.eq(infoId),
                        request.status.eq(status)
                )
                .fetch();
    }
    
    /**
     * 요청자 ID, 카테고리({@link InfoCategory}), 정렬 기준({@link InfoUpdateRequestSortType})에 따라
     * 커서 이후의 정보 수정 요청 목록을 조회하는 메서드
     *
     * <p> 정보 수정 요청은 별도의 제목 없이 연결된 정보의 제목으로 식별하므로, 정보 테이블과 조인해 정보의 카테고리·제목을 함께 조회한다.
     *     정보 수정 요청 엔티티 전체가 아닌, 목록 조회에 필요한 컬럼만 {@link InfoUpdateRequestSummaryViewProjection}으로 조회한다. </p>
     *
     * <ul>
     *     <li> 요청자 본인의 정보 수정 요청만 조회한다. </li>
     *     <li> 카테고리가 {@code null}이면 전체 카테고리를 조회한다. 카테고리는 연결된 정보의 카테고리를 기준으로 한다. </li>
     *     <li> 커서 값({@code cursorRequestedAt}, {@code cursorId})이 {@code null}이면 처음부터 조회한다. </li>
     * </ul>
     *
     * @param requesterId       정보 수정 요청자 ID
     * @param category          정보 카테고리
     * @param sortType          정렬 기준
     * @param cursorRequestedAt 마지막으로 조회한 정보 수정 요청의 요청 일시
     * @param cursorId          마지막으로 조회한 정보 수정 요청의 ID
     * @param limit             최대 조회 개수
     * @return 정보 수정 요청 요약 Projection 목록
     */
    @Override
    public List<InfoUpdateRequestSummaryViewProjection> findByCursor(
            Long requesterId,
            InfoCategory category,
            InfoUpdateRequestSortType sortType,
            LocalDateTime cursorRequestedAt,
            Long cursorId,
            int limit
    ) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        InfoUpdateRequestSummaryViewProjection.class,
                        request.id,
                        info.category,
                        info.title,
                        request.status,
                        request.requestedAt
                ))
                .from(request)
                .join(info).on(request.infoId.eq(info.id))
                .where(
                        request.requesterId.eq(requesterId),
                        categoryEq(category),
                        afterCursor(sortType, cursorRequestedAt, cursorId)
                )
                .orderBy(orderBy(sortType))
                .limit(limit)
                .fetch();
    }
    
    /**
     * 연결된 정보의 카테고리 조건을 반환하는 메서드
     *
     * @param category 정보 카테고리
     * @return 카테고리 조건, 카테고리가 {@code null}이면 조건을 적용하지 않도록 {@code null}
     */
    private BooleanExpression categoryEq(InfoCategory category) {
        return (category == null) ? null : info.category.eq(category);
    }
    
    /**
     * 정렬 기준에 따라 커서 이후의 정보 수정 요청만 조회하도록 하는 커서 조건을 반환하는 메서드
     *
     * <p> {@code requestedAt}이 같은 정보 수정 요청이 여러 개일 수 있으므로, {@code (requestedAt, id)} 순으로 비교한다.
     *     정렬 조건({@link #orderBy})과 같은 기준·방향으로 비교해야 중복·누락 없이 다음 목록을 조회할 수 있다. </p>
     *
     * <ul>
     *     <li> {@code LATEST}: 커서보다 요청 일시가 이전이거나, 요청 일시가 같고 ID가 작은 정보 수정 요청 </li>
     *     <li> {@code OLDEST}: 커서보다 요청 일시가 이후이거나, 요청 일시가 같고 ID가 큰 정보 수정 요청 </li>
     * </ul>
     *
     * @param sortType          정렬 기준
     * @param cursorRequestedAt 마지막으로 조회한 정보 수정 요청의 요청 일시
     * @param cursorId          마지막으로 조회한 정보 수정 요청의 ID
     * @return 커서 조건, 커서 값이 {@code null}이면 조건을 적용하지 않도록 {@code null}
     */
    private BooleanExpression afterCursor(
            InfoUpdateRequestSortType sortType,
            LocalDateTime cursorRequestedAt,
            Long cursorId
    ) {
        if (cursorRequestedAt == null || cursorId == null) {
            return null;
        }
        
        return switch (sortType) {
            case LATEST -> request.requestedAt.lt(cursorRequestedAt)
                    .or(request.requestedAt.eq(cursorRequestedAt).and(request.id.lt(cursorId)));
            case OLDEST -> request.requestedAt.gt(cursorRequestedAt)
                    .or(request.requestedAt.eq(cursorRequestedAt).and(request.id.gt(cursorId)));
        };
    }
    
    /**
     * 정렬 기준에 따른 정렬 조건을 반환하는 메서드
     *
     * <p> {@code requestedAt}이 같은 정보 수정 요청이 여러 개일 수 있으므로, 순서를 확정하기 위해 {@code id}를 보조 정렬 기준으로 사용한다. </p>
     *
     * <ul>
     *     <li> {@code LATEST}: 요청 일시 내림차순, ID 내림차순 </li>
     *     <li> {@code OLDEST}: 요청 일시 오름차순, ID 오름차순 </li>
     * </ul>
     *
     * @param sortType 정렬 기준
     * @return 정렬 조건 배열
     */
    private OrderSpecifier<?>[] orderBy(InfoUpdateRequestSortType sortType) {
        return switch (sortType) {
            case LATEST -> new OrderSpecifier<?>[]{request.requestedAt.desc(), request.id.desc()};
            case OLDEST -> new OrderSpecifier<?>[]{request.requestedAt.asc(), request.id.asc()};
        };
    }
}

package com.todayyu.persistence.info.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.persistence.info.dto.InfoUpdateRequestProjection;
import com.todayyu.persistence.info.entity.QInfoUpdateRequestJpaEntity;
import com.todayyu.persistence.member.entity.QMemberJpaEntity;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CustomInfoUpdateRequestRepositoryImpl implements CustomInfoUpdateRequestRepository {
    private final JPAQueryFactory jpaQueryFactory;
    
    private final QInfoUpdateRequestJpaEntity infoUpdateRequest = QInfoUpdateRequestJpaEntity.infoUpdateRequestJpaEntity;
    private final QMemberJpaEntity member = QMemberJpaEntity.memberJpaEntity;
    
    @Override
    public List<InfoUpdateRequestProjection> findWithRequesterNicknameByInfoIdAndStatus(
            Long infoId,
            InfoRequestStatus status
    ) {
        return jpaQueryFactory
                .select(
                        Projections.constructor(
                                InfoUpdateRequestProjection.class,
                                infoUpdateRequest,
                                member.nickname
                        )
                )
                .from(infoUpdateRequest)
                .join(member)
                .on(infoUpdateRequest.requesterId.eq(member.id))
                .where(
                        infoUpdateRequest.infoId.eq(infoId),
                        infoUpdateRequest.status.eq(status)
                )
                .fetch();
    }
}

package com.todayyu.persistence.info.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.todayyu.domain.info.vo.InfoRequestStatus;
import com.todayyu.persistence.info.dto.InfoRegistrationRequestProjection;
import com.todayyu.persistence.info.entity.QInfoRegistrationRequestJpaEntity;
import com.todayyu.persistence.member.entity.QMemberJpaEntity;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CustomInfoRegistrationRequestRepositoryImpl implements CustomInfoRegistrationRequestRepository {
    private final JPAQueryFactory jpaQueryFactory;
    
    private final QInfoRegistrationRequestJpaEntity infoRegistrationRequest
            = QInfoRegistrationRequestJpaEntity.infoRegistrationRequestJpaEntity;
    private final QMemberJpaEntity member
            = QMemberJpaEntity.memberJpaEntity;
    
    @Override
    public List<InfoRegistrationRequestProjection> findWithRequesterNicknameByInfoIdAndStatus(
            Long infoId,
            InfoRequestStatus status
    ) {
        return jpaQueryFactory
                .select(
                        Projections.constructor(
                                InfoRegistrationRequestProjection.class,
                                infoRegistrationRequest,
                                member.nickname
                        )
                )
                .from(infoRegistrationRequest)
                .join(member)
                .on(infoRegistrationRequest.requesterId.eq(member.id))
                .where(
                        infoRegistrationRequest.infoId.eq(infoId),
                        infoRegistrationRequest.status.eq(status)
                )
                .fetch();
    }
}
